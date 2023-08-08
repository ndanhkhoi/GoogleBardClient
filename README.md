#  Google Bard Client Library For Java [![](https://jitpack.io/v/ndanhkhoi/GoogleBardClient.svg)](https://jitpack.io/#ndanhkhoi/GoogleBardClient) [![gradle-publish](https://github.com/ndanhkhoi/GoogleBardClient/actions/workflows/gradle-publish.yml/badge.svg?branch=main)](https://github.com/ndanhkhoi/GoogleBardClient/actions/workflows/gradle-publish.yml)

Simple client to get `Google Bard`'s answer from your prompt

## Usage

Just import add the library to your project with one of these options:

1. Using Maven Central Repository:

- Step 1. Add the JitPack repository to your build file

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

- Step 2. Add the dependency

```xml
<dependency>
    <groupId>com.github.ndanhkhoi</groupId>
    <artifactId>GoogleBardClient</artifactId>
    <version>v2023.08.08</version>
</dependency>
```

2. Using Gradle:

- Step 1. Add the JitPack repository to your build file

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```

- Step 2. Add the dependency

```gradle
dependencies {
    implementation 'com.github.ndanhkhoi:GoogleBardClient:v2023.08.08'
}
```

## How to use

IIt's too easy with 2 lines of code:

```java
GoogleBardClient client = new GoogleBardClient(token);
String answer = client.chat("Hello");
```

## How to get token

- Login to your account in https://bard.google.com/
- Get value of the cookie named `__Secure-1PSID`

## How to reset conversation

```java
client.resetConversation();
```

## Dependencies

Thanks to these libraries:
1. [Lombok](https://github.com/projectlombok/lombok)
2. [Apache Commons Lang](https://github.com/apache/commons-lang)
3. [Apache Commons Text](https://github.com/apache/commons-text)
4. [Google Gson](https://github.com/google/gson)
5. [Slf4j](https://github.com/qos-ch/slf4j)

## License
MIT License

Copyright (c) 2023 Larry Deng

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.