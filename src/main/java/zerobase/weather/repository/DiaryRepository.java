package zerobase.weather.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.Diary;

import java.time.LocalDate;
import java.util.List;

//
@Repository
//DB와 맞닿아있는 기능
public interface DiaryRepository extends JpaRepository<Diary, Integer> {
    List<Diary> findAllByDate(LocalDate date);
    List<Diary> findAllByDateBetween(LocalDate startDate, LocalDate endDate);
    Diary getFirstByDate(LocalDate date);

    @Transactional
    void deleteAllByDate(LocalDate date);

    /*
    transaction이란?
    데이터베이스의 상태를 변화시키기 위해 수행하는 작업 단위

    작업단위 예시)
    트랜잭션: 오늘 일기 작성하기
    1) 오늘 날씨 데이터를 가져와서
    2) 일기를 db에 저장하기

    트랜잭션은 처리 과정 중 문제가 발생하면 작동하기 이전에 상태로 되돌림(롤백)

    작업들을 잘 분류해서, 하나의 트랜잭션으로 두고
    각각의 논리 구조에 따른 트랜잭션을 설계해두면
    트랜잭션 전체가 성공했을때만 데이터베이스 상태가 변경되게 된다.

    - 트랜잭션의 속성
    1. 원자성(Atomicity) : db에 모두 반영이되거나, 반영이 되지 않거나. 부분 반영은 없음
    2. 일관성(Consistency) : 트랜잭션의 처리 결과는 항상 일관적이어야 한다.
    3. 독립성(Isolation) : 하나의 데이터베이스에 여러 트랜잭션이 동작하면 문제가 발생할 수 있기 때문에
                        순차적으로 트랜잭션이 발생하도록 해주어야함
    4. 지속성(Durability) : 트랜잭션의 결과인 변화 상태가 쭉 지속되어야 함(데이터가 영구적으로 변경되어야 함)

    - 트랜잭션의 사용
    : 주로 DB에 문제가 생겼을 때 롤백시키기 위해 사용된다

    - 트랜잭션의 연산
     1) 커밋 : 처리 결과의 확정
     2) 롤백 : 문제 발생 시 처리 결과 없애고 이전 상태로 되돌림

     - 여러 트랜잭션이 경쟁하면 생기는 문제 -> 면접에서 물어볼 수 있어요
       트랜잭션 A: table의 3번째 row 수정 중
       트랜잭션 B: 테이블의 3번째 row 조회하려 함

       이런 상황에서 발생할 수 있는 문제는
       1) ditry read : A가 커밋하기 전에 데이터를 B가 조회한 경우 결과에 따라 B가 잘못된 데이터를 조회할 수 있음
       2) non-Repeatable Read : A가 row를 2번 조회 중이었는데, B가 중간에 3번째 row를 수정하고 커밋해버린 경우
                                -> A입장에선 1차 조회와 2차 조회 결과 값이 달라짐. 일관성을 해침
       3) Phantom read : 트랜잭션의 일관성을 해치는 경우. 예를 들어 A가 0~4번째 row를 2번 조회 중에
                        B 트랜잭션이 3번 row를 수정해버린다.

   - Spring 트랜잭션 세부 설정

   1. 어노테이션을 달아주는 경우: @Transactional
     - 클래스, 메서드 위에 추가할 수 있음
     - 어노테이션을 추가해주면 트랜잭션 기능이 적용된 프록시 객체 생성
       (PlatformTractionManager)
   2. spring transaction 세부 설정들
   * 일관성, 원자성을 보장하려고 하다보면 성능저하를 가져올 수 있음.
   개발자 입장에서는 어느것을 우선해야 하는 기능이냐에 따라 절대적인 일관성, 원자성을 보장하던가
   반대로 어느정도 용인하면서 성능을 우선시하겠다를 조율해야 함.

    1) Isolation(격리수준) : 트랜잭션에서 일관성이 없는 데이터를 허용하는 수준
      ① DEFAULT : 데이터베이스에서 사용하는 기본 격리수준을 따르겠다.
      ② READ_UNCOMMITTED : dirty read 발생 -> 상대적으로 가장 느슨한 격리 수준. Comitted 전에도 조회 가능
      ③ READ_COMMITTED : dirty read 방지. -> 확정 데이터만 읽기 허용
      ④ REPEATABLE_READ : non-repeatable read 방지 ->  트랜잭션이 완료 될 때 까지 사용되는 부분을 shared lock을 건다. 다른 사용자는 해당 영역에 조회 불가. -> 일관성 보장
      ⑤ SERIALIZABLE : phantom read 방지 -> 가장 엄격한 격리 수준
    2) Propagation(전파수준) :
    3) ReanOnly 속성 : 조회만 가능
    4) 트랜잭션 롤백 예외 :
    5) timeout 속성 :


    2. propagation(전파 수준)
: 트랜잭션 동작 도중 다른 트랜잭션을 호출하는 상황

- 트랜잭션을 시작하거나 기존 트랜잭션에 참여하는 방법에 대해 결정하는 속성값.

 1) Required : 기본 수준 a트랜잭션을 수행하는 도중 B트랜잭션을 수행했다면 A는 부모 B는 자식
   부모 트랜잭션을 수행하는 중에 자식 트랜잭션을 함께 수행하는데,
   부모 트랜잭션이 없는 경우에는 단독으로 실행

 2) supports : 이미 실행된 부모 트랜잭션이 있으면, 부모 트랜잭션에 참여를 하고 그렇지 않으면
      트랜잭션 없이 진행을 함
 3) requires_new : 부모 트랜잭션 안에서 자식 트랜잭션이 동작하는 경우에,
         부모 트랜잭션이 있어도 자식 트랜잭션을 위한 별도의 트랜잭션을 생성해서 진행
 4) nested : 이미 트랜잭션이 있는 경우에 중첩해서 실행 트랜잭션 안에 트랜잭션을 하나 더 만듦
      자식의 롤백/커밋 여부가 부모 트잭션에 영향을 안줌
      즉 부모가 롤백/커밋 하면 자식은 영향을 받는데 자식은 반대로 부모에 영향 안줌

    nested 예시: 일기 작성 관련해서 로그를 DB에 저장하는 상황
      1. 로그 저장이 실패한다고 해서 -> 일기 작성까지 롤백되면 안됨
      2. 일기 작성이 실패한다면 ->  로그 작성까지 롤백되어야 함
      일기 작성이 부모 트랜잭션, 로그가 자식 트랜잭션으로 진행되어야.

이 외에도 다양한 트랜잭션 옵션이 있어요

3.READ-only
: 트랜잭션을 읽기 전용 속성으로 지정
-> 성능을 최적화 하기 위해서
-> 읽기 외에 쓰기, 삭제, 수정을 방지하기 위해서
사용한다.

기본적으로는 false로 설정되어 있음

@Transactional(readOnly = true)

4. 트랜잭션 롤백 예외
: 예외 발생했을 때 트랜잭션 롤백시킬 경우를 설정

@Transactional(rollbackFor=Exception.class) : 모든 에러에 대해서 롤백
@Transactional(norollbackFor=Exception.class) : 어떤 에러가 발생하더라도 롤백하지 않음

defalut: runtimeException, Error

5. timeout속성
: 일정 시간 내에 트랜잭션 끝내지 못하면 롤백
@Transactional(timeout = 10)

데이터베이스에 문제가 있거나 해서 트랜잭션 끝내지 못하고 계속 돌아가면 롤백

격리수준이 빡빡한 경우에 써주면 좋고, 격리 수준이 빡빡하지 않아도 오랜 시간 지연되면 타임아웃.
계속 트랜잭션이 돌아가면 서버의 성능이 저하되니까.


08.01 Spring boot Logging이란?

1. Log의 사용 이유
 1) 서비스 동작 상태 파악
 2) 장애 파악

2. 작성하는 방법
 1) System.out.println("로그 내용~") : 코드가 길어지고, 많은 로그들은 코드 안에 심다 보니 가독성도 떨어짐. 로그 레벨도 모름
 2) Logging library 사용

3. logging library 종류
 - log4j -> log4j2
 - logback
 log4j -> logback -> log4j2 순으로 나옴

최신으로 나온 친구일 수록 기능이 더 많아요.
근데 어느정도 기본 기능들은 다 갖추고 있음

4. 로그 레벨 : 로그 레벨에 따라서 로그들을 쓸 수 있도록 기능을 제공 라이브러리마다 약간의 차이는 있다.
 1) Error : 가장 중요한 로그 레벨. 심각한 장애가 발생한 경우 즉시 조치를 취해야 한다.
 2) warn : 로직상 유효성을 사용할 때 써요. 예상 가능한 장애를 체크 할 때 쓰는 것. 당장 문제가 되지는 않으나, 높은 확률로 문제가 예상된다
 3) info : 서비스를 운영하는데 참고할 만한 중요한 작업이 끝났다거나 할 때 INFO로그.
 4) debug
 5) trace
 4), 5)는 개발 단계에서 세세하게 남기고 싶은 로그를 쓸 때 사용. 1~3은 실제 서비스를 운영할 때 많이 씀.
 정말 정말 중요한 문제는 에러라고 둔다는게 제일 중요

09.01 예외 처리하기
예측할 수 없는 에러 수준들을 예외로 처리함

1. 활용할 수 있는 기본적인 예외처리
 1) try/catch문
 2) custom exception 만들어서 처리

 1번의 방법으로는 exception을 다 처리하는 것은 현실적으로 어렵다

2. Exception handler란?
@Controller. @RestController의 예외를 하나의 메서드에서 처리해주는 기능
유의사항: 하나의 컨트롤러에 핸들러를 지정해주면 그 핸들러는 컨트롤러 안에 있는 exception만 잡아줌

3. ControllerAdvice
모든 controller단에서 발생하는 예외가 발생한 것을 잡아줌 -> 현실적으로는 얘가 필수적!

10.01 API DOCUMENT만들기
 1. 왜 도큐멘트 작성해야 하나요?
  1) 프론트 개발자에게 문서를 전해야 함
  2) 백엔드 개발자끼리 공유해야 함
 2. api 문서화 방식
 1) txt 파일에 정리한다 -> 실제로는 쓰기 어려워요. 수정사항을 추적하기 어렵기 때문. 가독성도 안좋아.
   - GET /read/diary
   - POST /create/diary

 2) api documentation을 돕는 tool들
  ㄱ. swagger - API 문서화를 도와주는 툴 중에서 Html로 접근 가능하다는 장점이 있음. 하나의 url로 남들에게 공유 가능. 보기도 예뻐. 호출도 바로 해볼 수 있다.
  ㄴ. reDoc
  ㄷ. GitBook
  ㄹ. ...

 다양한 툴이 있음 여러 의사결정 과정 또는 툴의 언어 친화적인 특성 , 혹은 필요로 하는 HTTP 요청 방식 등에 따라 툴을 선택하는 기준이 달라질 것



     */
    /*
      트랜잭션 코드에 반영하기
      @Transactional
      @EnableTransactionManagement -> 붙여준 클래스 안에 트랜잭셔널이 동작하도록 함.
                                   -> 트랜잭션 쓰면 꼭 같이 써주세요
                                   -> main 함수가 있는 Springbootapli 클래스에 붙여주면 됨

      db 작업을 하는 트랜잭션 메소드가 많아지면 일일히 지정해주기가 어렵다.
      그런 경우에 클래스에 트랜잭션을 어노테이션으로 달아줘요
      -> 각각의 메소드를 트랜잭션 어노테이션을 붙여준 효과

      만약 클래스에 붙어있는 트랜잭션 어노테이션과 메소드의 트랜잭션이 충돌하는 상황이라면
      어노테이션이 붙어있는 메소드 먼저 -> 그다음에 클래스로

      일반적으로 service단에 trasactional을 많이 쓴다.


       */

}
