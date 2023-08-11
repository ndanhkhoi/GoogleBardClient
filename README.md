#  Google Bard Client Library For Java [![](https://jitpack.io/v/ndanhkhoi/GoogleBardClient.svg)](https://jitpack.io/#ndanhkhoi/GoogleBardClient) 
Simple client to get `Google Bard`'s answer from your prompt

## Usage

Just import the library to your project with one of these options:

### Using Maven Central Repository:

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
    <version>v2023.08.11</version>
</dependency>
```

### Using Gradle:

- Step 1. Add the JitPack repository to your build file

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```

- Step 2. Add the dependency

```gradle
dependencies {
    implementation 'com.github.ndanhkhoi:GoogleBardClient:v2023.08.11'
}
```

## How to use

IIt's too easy with few lines of code:

```java
public class BardClientApp {

    public static void main(String[] args) {
        String secure1psid = "Your __Secure-1PSID Cookie";
        String secure1psidts = "Your __Secure-1PSIDTS Cookie";
        GoogleBardClient client = new GoogleBardClient(secure1psid, secure1psidts);
        Result result = client.chat("Hello");
        if (result != null) {
            System.out.println(result.getContent());
            result.getImages().forEach(e -> System.out.println("Image link: " + e));
        }
    }
    
}
```

## How to get Cookies

- Login to your account in https://bard.google.com/
- Get value of the cookie named `__Secure-1PSID`
- Get value of the cookie named `__Secure-1PSIDTS`

## How to reset conversation

```java
public class BardClientApp {

    // your code
    
    public void exampleForResetConversation() {
        client.resetConversation();
    }

}
```

## Dependencies

### Thanks to these libraries:
1. [Okhttp](https://github.com/square/okhttp)
2. [Gson](https://github.com/google/gson)
3. [Slf4j](https://github.com/qos-ch/slf4j)

### Thanks to these reơpsitories:
1. [acheong08/Bard](https://github.com/acheong08/Bard): Reverse engineering of Google's Bard chatbot API

## License
MIT License

Copyright (c) 2023 Nguyễn Đức Anh Khôi

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
