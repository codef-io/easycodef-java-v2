<h1 align="center">EasyCodef Java V2</h1>
<br>
<p align="center">
  <a title="코드에프" href="https://codef.io/">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://github.com/user-attachments/assets/d83f0450-d84e-4594-8fc0-ed08a1d05390">
      <img alt="코드에프" src="https://github.com/user-attachments/assets/d83f0450-d84e-4594-8fc0-ed08a1d05390" width="250">
    </picture>
  </a>
</p>

<br>

`easycodef-java-v2`는 codef API를 JDK 환경에서 더욱 더 편리하게 연동할 수 있도록 돕는 오픈소스 라이브러리입니다.

현재 알파 버전 개발중으로 v2.0.0-ALPHA-002 버전으로 Maven Central Repository를 통해 배포중입니다.

2024년 상반기 실제 고객사 대상으로 릴리즈 예정입니다.

## Release

[![Build Status](https://img.shields.io/github/actions/workflow/status/codef-io/easycodef-java-v2/publish.yml?style=for-the-badge&logo=gradle&color=02303A)](https://github.com/codef-io/easycodef-java-v2/actions?query=branch%3Amaster)<br>
[![Last Commit](https://img.shields.io/github/last-commit/codef-io/easycodef-java-v2/master?style=for-the-badge&label=LAST%20BUILD&logo=Github&color=181717)](https://github.com/codef-io/easycodef-java-v2)<br>
[![Maven Central](https://img.shields.io/maven-central/v/io.codef.api/easycodef-java-v2.svg?style=for-the-badge&label=Maven%20Central&logo=apache-maven&color=C71A36)](https://central.sonatype.com/artifact/io.codef.api/easycodef-java-v2)<br>

## Snippets

- Gradle(Kotlin)
    ```gradle
    implementation("io.codef.api:easycodef-java-v2:2.0.0-alpha-002")
    ```
  
- Gradle(short)
    ```gradle
    implementation 'io.codef.api:easycodef-java-v2:2.0.0-alpha-002'
    ```
  
- Maven
    ```xml
    <dependency>
        <groupId>io.codef.api</groupId>
        <artifactId>easycodef-java-v2</artifactId>
        <version>2.0.0-alpha-002</version>
    </dependency>
    ```

## Get It !

- 예제 코드
  ```java
  EasyCodef easyCodef = EasyCodefBuilder.builder()
          .clientType(CodefClientType.DEMO)
          .clientId("your-client-id")
          .clientSecret("your-client-secret")
          .publicKey("your-public-key")
          .build();
  
  EasyCodefRequest request = EasyCodefRequestBuilder.builder()
          .path("/v1/kr/public/hw/nip-cdc-list/my-vaccination")
          .organization("0011")
          .requestBody("loginType", "1")
          .requestBody("userId", "your-nhis-id")
          .secureRequestBody("userPassword", "your-nhis-password")
          .secureWith(easyCodef)
          .build();
  
  EasyCodefResponse easyCodefResponse = easyCodef.requestProduct(request);
  
  final EasyCodefResponse.Result result = easyCodefResponse.result();
  final Object data = easyCodefResponse.data();
  ```

---

<p align="center">
<img alt="헥토데이터" src="https://github.com/user-attachments/assets/ac6b7a7d-33f1-4b1e-9fbb-8231d56e7f33" height="20"><br>
<span>MIT © | <a href="https://github.com/codef-io/easycodef-java-v2/blob/master/LICENSE" target="_blank">LICENSE</a></span>
</p>

