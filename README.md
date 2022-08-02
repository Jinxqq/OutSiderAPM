

## **OutSiderAPM移动性能监控平台（持续开发中）**

### **项目优势**

- 实时掌控应用性能
- 降低性能定位成本
- 有效提升用户体验

### **监控模块**

OutSiderAPM目前支持如下性能指标：

- 交互分析：分析Activity生命周期耗时，帮助提升页面打开速度，优化用户UI体验
- 网络请求分析：监控流量使用情况，发现并定位各种网络问题
- 内存分析：全面监控内存使用情况，降低内存占用
- 进程监控：针对多进程应用，统计进程启动情况，发现启动异常（耗电、存活率等）
- 文件监控：监控APP私有文件大小/变化，避免私有文件过大导致的卡顿、存储空间占用等问题
- 卡顿分析：监控并发现卡顿原因，代码堆栈精准定位问题，解决明显的卡顿体验
- ANR分析：捕获ANR异常，解决APP的“未响应”问题

### **OutSiderAPM特性**

- **非侵入式**

​	无需修改原有工程结构，无侵入接入，接入成本低。

- **无性能损耗**

​	OutSiderAPM针对各个性能采集模块，优化了采集时机，在不影响原有性能的基础上进行性能的采集和分析。

- **监控全面**

​	目前支持UI性能、网络性能、内存、进程、文件、卡顿、ANR等各个维度的性能数据分析，后续还会继续增加新的性能维度。

- **Debug模式**

​	独有的Debug模式，支持开发和测试阶段、实时采集性能数据，实时本地分析的能力，帮助开发和测试人员在上线前解决性能问题。

- **支持插件化方案**

​	在初始化阶段进行设置，可支持插件接入，并且性能方面无影响。

- **支持多进程采集**

​	针对多进程的情况，我们做了相应的数据采集及优化方案，使OutSiderAPM既适合单进程APP也适合多进程APP。

- **节省用户流量**

​	OutSiderAPM使用wifi状态下上传性能数据，这样避免了频繁网络请求带来的耗电问题及用户流量的消耗。





### **Usage**

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
	implementation 'com.gitee.HappyAndroid666:OutSiderAPM:1.0.0'
}
```