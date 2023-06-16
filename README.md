# Journey_Memory

## 旅遊日誌簡介
這是我們的Android期末專案，是一個旅遊日誌<br>
可以選擇日誌類別，編輯日誌標題，在某段時間創建屬於您的日誌!<br>
日誌中可以以cell形式加入圖片、文字、地點、錄音等(像是jupyter notebook)<br>
您隨時都可以查看您的日誌並進行修改!

## 使用說明

### a. 使用環境

+ 基本需求：
    + Android Studio Electric Eel | 2022.1.1 Patch 2
    + Android SDK 30
    + Android 11

+ 建議環境：
    + Android Studio Electric Eel | 2022.1.1 Patch 2
    + Android SDK 33
    + Android 12

### b. 使用方式

+ 透過 android studio 開啟專案，並透過模擬器或是實體手機執行
+ 以 android 手機或模擬器下載 [JourneyMemory.apk](https://raw.githubusercontent.com/Jayyyu1w/Journey_Memory/main/JourneyMemory.apk) 並安裝


## 功能說明
!!建議開啟聲音以獲得更好的使用體驗!!

### 主頁說明
首先進入"旅遊日誌"可以看到主頁面


<img src="https://github.com/Jayyyu1w/Journey_Memory/blob/main/src/main_page.png?raw=true" alt="test" width="200">


點擊第一個畫面框，可以選擇您要的"日誌類別"(旅遊、美食、其他)

點擊第二個欄位並輸入您的"日誌標題"

點選"開始日期"與"結束日期"按鈕直接選擇您想創建的日誌適用時間(開始旅程前必填欄位)

填選完畢即可點擊"開始旅程"即可開始編輯您的日誌

若點選"旅程回憶"即可回顧您所創建的日誌

若點選"分享"即可和好友分享我們的App

### 開始旅程頁說明
進入"開始旅程"創建您的旅程


<img src="https://github.com/Jayyyu1w/Journey_Memory/blob/main/src/edit_page.png?raw=true" alt="test" width="200">


點選右下"+"圖示可展開選擇您想要加入日誌的種類

點選右下"打勾"圖示可儲存您的日誌

點擊您的日誌標題可進行標題更改

長按您加入的任何日誌元素都可以進行刪除

#### 地圖功能

點擊"地圖圖示"可選擇"手動"或"自動"加入地點名稱或座標

+ 手動：輸入地標名稱，並點擊"確定"即可加入地點名稱
+ 自動：自動加入您當前位置的經緯度

點擊"地點名稱或座標"，將自動打開Google Map並查看此位置的地圖資訊

實作細節是透過 Place API 取得地點資訊，並以 Google Map API 顯示地圖

#### 錄音功能
點擊"錄音圖示"可以創建錄音

點擊第一下出現的"錄音按鈕"即可進行錄音

點擊第二下可以停止錄音

之後再點擊即可撥放錄音

#### 文字功能
點擊"文字圖示"可以創建可輸入文字的文字框，並輸入您想要的文字

#### 空格功能
點擊"空格圖示"可讓您在元素和元素之間留空，方便進行排版

#### 相機功能
點擊"相機圖示"可幫您打開相機進行相片拍攝並加入日誌中

#### 相片功能
點擊"相片圖示"可讓您選擇相簿中的照片並加入日誌中

### 旅程回憶頁說明
進入"旅程回憶"回顧您的的旅程


<img src="https://github.com/Jayyyu1w/Journey_Memory/blob/main/src/memory_page.png?raw=true" alt="test" width="200">


點擊日誌中出現的日曆並選擇日期，可查看您在那段時間所創建的日誌

點擊出現的日誌可以查看並編輯您的日誌內容

長按日誌可以永久刪除日誌

### 分享頁說明
進入"分享"將我們的App分享給所有人知道!


<img src="https://github.com/Jayyyu1w/Journey_Memory/blob/main/src/share_page.png?raw=true" alt="test" width="200">


## Android Final Project
This is our Android final project, a travel journal app.

You can select a journal category, edit the journal title, and create your own journal during a specific period of time!

In the journal, you can add images, text, locations, recordings, etc., in the form of cells (similar to Jupyter Notebook).

You can view and modify your journal anytime!

<img src="https://github.com/Jayyyu1w/Journey_Memory/blob/main/src/test.png?raw=true" alt="test" width="300">
