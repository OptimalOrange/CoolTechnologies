炫科技
================

[![Build Status](https://travis-ci.org/OptimalOrange/CoolTechnologies.svg?branch=master)](https://travis-ci.org/OptimalOrange/CoolTechnologies)

功能
-------------

* 热门推荐
* 类型
* 评论（登录）
* 收藏
* 播放记录（本地）
* 搜索
* 推荐

Build
-------------

一些常用Gradle命令（任务task）：

 * `gradlew clean build` - 清除旧的build目录并重新构建测试整个项目
 * `gradlew check` - 运行所有检查（build包含check）
 * `gradlew installDebug` - 把 debug apk 安装到当前连接的设备上
 * `gradlew connectedAndroidTest` - 在连接的设备上安装并运行测试
 * `gradlew tasks` - 显示本项目中可用的Gradle任务及其说明

> Tip:<br/>
> 在没有冲突的情况下，Gradle支持task名缩写。<br/>
> 例如：gradle installDebug可以缩写为gradle iD（如果没有其他缩写为iD的任务）。

LICENSE
-------------

    Copyright 2014 OptimalOrange

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
