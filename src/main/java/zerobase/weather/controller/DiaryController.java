package zerobase.weather.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import zerobase.weather.domain.Diary;
import zerobase.weather.service.DiaryService;

import java.time.LocalDate;
import java.util.List;

// controller는 외부(client)로부터 받는 응답 또는 보내는 요청 등등에 관련된
// 가장 맞닿아있는 클래스에여
// 컨트롤러가 일기 어플에 어떤 api를 제공해주어야 하는가?

@Tag(name = "날씨 일기 프로젝트", description = "일기 관련 CRUD 동작 확인 관련 백엔드 API")
@RestController // 기본 컨트롤러 + http 상태코드를 지정해서 내려줌
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    // api의 경로(path)를 지정해주자
    @Operation(summary = "날씨 일기 신규 작성", description = "작성을 원하는 날짜 입력 후 원하는 내용 작성시 해당 일자에 신규 일기 저장")
    @PostMapping("/create/diary") // get은 조회할 때 많이 쓰고 post는 저장할 때 많이 써요
     void createDiary(
             @Parameter(name = "date", description = "YYYY-MM-DD", example = "2024-01-01", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate date,  //@DateTimeFormat 쓴 이유 : 년월일에 대한 사람마다 표기 방식이 다를 수 있기 때문에 표기의 통일을 위해 포맷 지정
            @RequestBody String text // requestparam : 파라미터 형식으로 입력하겠다 @Requestbody : 바디값으로 보내줄 내용(http: 에서 body 값에 이걸 보내겠다)
            ){
        diaryService.createDiary(date, text);


    }
    @Operation(summary = "기존 날씨 일기 조회", description = "조회를 원하는 날짜 입력 시 기존 저장된 일기 조회")
    @GetMapping("/read/diary")
    List<Diary> readDiary
            (@Parameter(name = "date", description = "YYYY-MM-DD", example = "2024-01-01", required = true)
                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate date){
       return diaryService.readDiary(date);
    }


    @Operation(summary = "원하는 기간동안의 기존 날씨 일기 조회", description = "조회를 원하는 기간 입력 시 기존 저장된 일기 조회")
    @GetMapping("/read/diaries")
    List<Diary> readDiaries
            (@Parameter(name = "startDate", description = "YYYY-MM-DD", example = "2024-01-01", required = true)
                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate startDate,
             @Parameter(name = "endDate", description = "YYYY-MM-DD", example = "2024-01-31", required = true)
             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate endDate){


        return diaryService.readDiaries(startDate, endDate);
    }
    @Operation(summary = "기존 날씨 일기 수정", description = "수정을 원하는 날짜를 입력 후 수정 내용 작성시 일기 내용 수정")
    @PutMapping("/update/diary")
    void updateDiary(
            @Parameter(name = "date", description = "YYYY-MM-DD", example = "2024-01-01", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate date,
            @RequestBody String text
    ){
        diaryService.updateDiary(date, text);

    }

    @Operation(summary = "기존 날씨 일기 삭제", description = "삭제을 원하는 날짜를 입력 시 기존 일기 삭제")
    void deleteDiary(
            @Parameter(name = "date", description = "YYYY-MM-DD", example = "2024-01-01", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate date
    ){
        diaryService.deleteDiary(date);
    }

}
