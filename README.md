### ※ <b>Python Classification Server</b> ※: https://github.com/millennium-diary/PictureDiary-Classification-Server
<br>

# Picture Diary
본 프로젝트는 어린아이들의 <b>그림을 인식하여 개선된 이미지로 대체</b>할 수 있고, 일기에 관심을 더 가질 수 있도록 <b>그림에 움직임을 부여</b>함으로써 보다 생동감 넘치는 효과를 제공하는 것이 주목적이며, 나아가 이를 <b>다른 사람과 함께 공유할 수 있는 환경을 구축</b>하기 위한 프로젝트이다.
<br><br>

# 개발 환경
<table>
  <tr>
    <td rowspan="4"><b>&nbsp;&nbsp;&nbsp;PC&nbsp;&nbsp;&nbsp;</td>
    <td>운영체제</td>
    <td>Windows 11</td>
  </tr>
  <tr>
    <td>CPU</td>
    <td>Intel Core i5-1130G7</td>
  </tr>
  <tr>
    <td>RAM</td>
    <td>16GB 4266MHz LPDDR4X SDRAM</td>
  </tr>
  <tr>
    <td>소켓 서버</td>
    <td>Python 3.9.7</td>
  </tr>
</table>

<table>
  <tr>
    <td rowspan="4"><b>모바일</td>
    <td>운영체제</td>
    <td>Android 11</td>
  </tr>
  <tr>
    <td>개발 언어</td>
    <td>Kotlin</td>
  </tr>
  <tr>
    <td>프레임워크</td>
    <td>Android Studio 4.1.2</td>
  </tr>
  <tr>
    <td>데이터베이스</td>
    <td>Firestore Database, SQLite(내장)</td>
  </tr>
</table>
<br><br>

# 시스템 구성도
![image](https://user-images.githubusercontent.com/62047373/175225616-55651129-b3cc-48d1-a316-2835d03bf9e8.png)
<br><br>

# 시스템 흐름도
![image](https://user-images.githubusercontent.com/62047373/175225709-7b53910a-dbb1-45ca-8330-81c41f5a3a32.png)
<br><br>

# 실행 화면
![image](https://user-images.githubusercontent.com/61930770/187321541-5feaf08f-90b4-47df-b2e5-93d142d6e03e.png)
![image](https://user-images.githubusercontent.com/61930770/187321635-42122c3d-f2c3-4a0c-9f81-bb72a6ae76e9.png)
![image](https://user-images.githubusercontent.com/61930770/187321656-15d97824-14d1-4496-a7f5-eefb1777bdeb.png)
![image](https://user-images.githubusercontent.com/61930770/187321782-5e4d1d5f-4d9c-486a-8bcf-d79fdceeb2ae.png)
![image](https://user-images.githubusercontent.com/61930770/187321959-938ac9e4-ae4b-4496-979f-6310948404cf.png)

<!--![image](https://user-images.githubusercontent.com/62047373/175224502-8c29c818-9851-4ba0-ab37-e16625e6c7a7.png)-->
<!--![image](https://user-images.githubusercontent.com/62047373/175224521-3ed4908e-2f39-42f1-b57a-30cd2f8ee113.png)-->
<br><br>

# 역할 분담
* <a href="https://github.com/yeonsu10">김연수</a> (jenix1@tukorea.ac.kr)
  - 조장: 팀 회의 주관
  - SNS 방식의 공유 플랫폼 기능 및 UI 구현
  - 파이어스토어 데이터베이스 구현
  - 전체적인 세부사항 개선
  
* <a href="https://github.com/nhk1657">노현경</a> (nhk1657@tukorea.ac.kr)
  - 그림판 UI 구현
  - 그림판 크롭 기능 및 UI 구현 
  - 캘린더 기능 구현
  
* <a href="https://github.com/ddubidubap">박성연</a> (2019150019@tukorea.ac.kr)
  - 로그인 기능 구현
  - 캘린더 기능 구현
  - 전체 데이터베이스 구현
  - 그림 인식 모델 구축
  - 이미지 크롤링
  - 서버 - 클라이언트 소켓 프로그래밍
  - 전체적인 흐름 관리
  
* <a href="https://github.com/LHY00y">이하영</a> (leeha0507@tukorea.ac.kr)
  - 그림판 기능 및 UI 구현
  - 모션 기능 구현
  - 적용 가능한 모션 개발
 
<br><br>

![Kotlin: Version](https://img.shields.io/badge/Kotlin-green)
![Python: Version](https://img.shields.io/badge/Python-blue)
![Firebase: Version](https://img.shields.io/badge/Firebase-orange)
