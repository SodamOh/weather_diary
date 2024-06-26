package zerobase.weather.service;



import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.WeatherApplication;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional(readOnly = true) //import 할 때 javax말고 springframework에 있는 거 쓰세여
@Service
public class DiaryService {

    /**
     * open weather_map 에서 데이터 받아오기
     * 받아온 날씨 데이터 파싱하기
     * 우리 DB에 저장하기
     */

    @Value("${openweathermap.key}")
    private String apiKey;
    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;
    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);

    public DiaryService(DiaryRepository diaryRepository, DateWeatherRepository dateWeatherRepository) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void createDiary(LocalDate date, String text) {
        logger.info("started to create diary");
        // 날씨 데이터 가져오기(API or DB에서 가져오기)
        DateWeather dateWeather = getDateWeather(date);

//      //파싱된 데이터 + 일기 값 DB에 넣기
        Diary nowDiary = new Diary();
        nowDiary.setDateWeather(dateWeather);
        nowDiary.setText(text);
        nowDiary.setDate(date);


//        // 날씨 데이터 가져오기(API에서 가져오기)
//        String weatherData = getWeatherString();
//
//        // 받아온 날씨 json 파싱하기
//        Map<String, Object> parsedWeather = parseWeather(weatherData);
//
//        //파싱된 데이터 + 일기 값 DB에 넣기
//        Diary nowDiary = new Diary();
//        nowDiary.setWeather(parsedWeather.get("main").toString());
//        nowDiary.setIcon(parsedWeather.get("icon").toString());
//        nowDiary.setTemperature((double) parsedWeather.get("temp"));
//        nowDiary.setText(text);
//        nowDiary.setDate(date);

        diaryRepository.save(nowDiary);
        logger.info("end to create diary");

    }
    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
      //  logger.debug("read diary");
//        if(date.isAfter(LocalDate.ofYearDay(3050,1))) {
//            throw new InvalidDate();
//        }
       return diaryRepository.findAllByDate(date);
    }
    @Transactional(readOnly = true)
    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    public void updateDiary(LocalDate date, String text) {
        Diary nowDiary = diaryRepository.getFirstByDate(date);
        nowDiary.setText(text);
        diaryRepository.save(nowDiary);
    }

    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }

    @Transactional
    @Scheduled(cron = "0 0 1 * * *") // 초 분 시 매일 매월 // 0/5슬래스의 이미: per 5sec
    public void saveWeatherDate(){
        logger.info("오늘도 날씨 데이터 잘 가져옴");
        dateWeatherRepository.save(getWeatherFromApi());
    }

    public String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + apiKey;


        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // 위에 url을 http 요청을 할 수 있게 보내주는 역할
            connection.setRequestMethod("GET"); // 보내는 메소드는 get으로 하겠다
            int responseCode = connection.getResponseCode();// code는 200 400 이런거니까 int 값으로 받아온다.

            BufferedReader br; // 응답코드에 따라서 응답객체를 받아오고 br에 저장한다.

            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream())); // 어떤 오류인지 보기 위해서
            }
            String inputLine;
            StringBuilder response = new StringBuilder(); // br에 넣어두었던걸 하나하나 읽으면서 response라는 스트링빌더에
            while ((inputLine = br.readLine()) != null) { // 결과값을 하나하나 쌓는다.
                response.append(inputLine);
            }
            // 응답 또는 에러 코드가 길 수 있기 때문에 bufferedreader를 사용하면 긴 코드도 빠르게 받을 수 있어요.


            br.close();

            return response.toString();
        } catch (Exception e) {
            return "failed to get response";
        }
    }

    //json 파싱을 위한 메소드
    private Map<String, Object> parseWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);//파싱한 결과 값 담아주기

        } catch (ParseException e) {
            throw new RuntimeException(e); // 걍 에러가 났다. 요렇게만 처리되게
        }
        Map<String, Object> resultMap = new HashMap<>();

        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp")); // main 안에 있는 temp를 가져오기 위해서
        JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherArray.get(0); // api doc에서 가져오고자 하는 값이 정의된 response를 지정해준다.
        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));
        System.out.println(getWeatherString());
        return resultMap;

    }

    // 자정마다 날씨 데이터를 가져와 줄 메서드
    private DateWeather getWeatherFromApi(){
        // 날씨 데이터 가져오기
        String weatherData = getWeatherString();

        // 받아온 날씨 json 파싱하기
        Map<String, Object> parsedWeather = parseWeather(weatherData);

        // 파싱 된 날짜 데이터를 db에 저장
        DateWeather dateWeather = new DateWeather();
        dateWeather.setDate(LocalDate.now());
        dateWeather.setWeather(parseWeather(weatherData).get("main").toString());
        dateWeather.setIcon(parseWeather(weatherData).get("icon").toString());
        dateWeather.setTemperature((Double)parseWeather(weatherData).get("temp"));


        return dateWeather;
    }

    private DateWeather getDateWeather(LocalDate date){
        List<DateWeather> dateWeatherListFormDB = dateWeatherRepository.findAllByDate(date);
        if(dateWeatherListFormDB.isEmpty()){
            //새로 api에서 날씨 정보를 가져와야 한다.
            // 현재 날씨를 가져오도록 정책을 정했다고 가정하자
            return getWeatherFromApi();
        }else{
            return dateWeatherListFormDB.get(0);
        }
    }


}
