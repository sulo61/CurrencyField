# CurrencyField
AppCompatEditText with auto formatting currencies. 
Current support:
- USD
- EUR

## 0. Preview
![Imgur](https://i.imgur.com/zKtBOIi.gif)

## 1. Dependency
```
implementation 'io.sulek:currencyfield:1.0.2'
```
```
repositories {
    maven {
        url  "https://dl.bintray.com/sulo61/Android"
    }
}
```

## 2. Usage
#XML
```
<io.sulek.currencyfield.CurrencyField
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:currency="USD" />
```

## 3. Licence

```
Copyright 2019 Michał Sułek

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```