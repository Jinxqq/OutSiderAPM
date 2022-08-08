## ASM对匿名内部类、Lambda及方法引用的Hook研究



ASM在安卓开发中的应用十分广泛，本文重点探讨通过ASM对匿名内部类、Lambda表达式及方法引用的Hook。

安卓的编译流程中Java文件会被编译成`.class`，`.class`会被编译成`.dex`。而ASM的执行时机就是在`.class`编译成`.dex`的过程中发生的。因此要想通过ASM修改自己码就需要知道我们的Java文件编译成的`.class`是怎样的。

PS：本文假设你对ASM有一定了解。

### 一，匿名内部类方式

我们在面试时经常会说起handler的内存泄漏问题，原因是匿名内部类默认会持有外部类的引用，因此巴拉巴拉。。。

那么匿名内部类为什么会持有外部类的引用，编译后又是什么样子呢？我们撸代码看下。

写一段简单的启动线程的代码：

```java
public class FuncActivity {
    
    private void test1(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("FuncActivity", "thread - new Runnable");
            }
        }).start();
    }
}
```

下面来看下其编译后的产物。

##### 1，匿名内部类编译后的.class文件

匿名内部类会生成一个新的`.class`文件，命名格式为：`外部类类名$序号.class`。

首先查看一下生成的匿名内部类的.class代码：

```java
class FuncActivity$1 implements Runnable {
    FuncActivity$1(final FuncActivity this$0) {
        this.this$0 = this$0;
    }

    public void run() {
        Log.e("FuncActivity", "thread - new Runnable");
    }
}
```

自动生成的`FuncActivity$1`实现了`Runnable`接口，其构造方法传入了外部类`FuncActivity`的对象，因此匿名内部类持有了外部类的引用。

##### 2，外部类ASM代码

通过Android Studio查看编译后的.class文件，发现编辑器对其做了反编译：

```java
public class FuncActivity {

    private void test1() {
        (new Thread(new Runnable() {
            public void run() {
                Log.e("FuncActivity", "thread - new Runnable");
            }
        })).start();
    }
}
```

通过**Byte Code Analyzer**插件可以查看对应ASM代码。

```java
{
methodVisitor = classWriter.visitMethod(ACC_PRIVATE, "test1", "()V", null, null);
methodVisitor.visitCode();
...
// new Thread
methodVisitor.visitTypeInsn(NEW, "java/lang/Thread");
// 在操作数栈中复制上面new Thread的对象
methodVisitor.visitInsn(DUP);
// new 自动生成的匿名内部类(FuncActivity$1)
methodVisitor.visitTypeInsn(NEW, "me/wsj/performance/ui/FuncActivity$1");
// 复制上面的匿名内部类对象
methodVisitor.visitInsn(DUP);
// 加载当前class对象(FuncActivity.this)
methodVisitor.visitVarInsn(ALOAD, 0);
// FuncActivity$1初始化(执行init)，传入FuncActivity.this对象
methodVisitor.visitMethodInsn(INVOKESPECIAL, "me/wsj/performance/ui/FuncActivity$1", "<init>", "(Lme/wsj/performance/ui/FuncActivity;)V", false);
// new Thread对象初始化，传入FuncActivity$1对象
methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Thread", "<init>", "(Ljava/lang/Runnable;)V", false);
...
// 执行线程的start方法
methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "start", "()V", false);
...
methodVisitor.visitInsn(RETURN);
...
methodVisitor.visitEnd();
}
```

每行代码的含义已经大概注释出，关键点就是创建了一个`FuncActivity$1`对象，并将其作为参数创建一个`Thread`，最后执行了`Thread`对象的`start()`方法。

##### 3，植入代码

明白了代码编译后的大概执行逻辑就可以对其进行hook植入代码。

通过重写`ClassVisitor`的`visitMethod()`方法，在其中根据方法的`name`及`descriptor`做过滤即可找到需要hook的Method：

```kotlin
class TrackerClassAdapter(api: Int, cv: ClassVisitor?) : BaseClassVisitor(api, cv) {
    
	override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?,
        mv: MethodVisitor
    ): MethodVisitor {
        // 找到需要hook的Method
        if (isRunMethod(name, descriptor)) {
            // 自定义MethodVisitor
            return TrackerMethodAdapter(descriptor, api, access, mv)
        }
        return mv
    }
	
    // 根据方法的name及descriptor做判断
    fun isRunMethod(name: String, descriptor: String?): Boolean {
        return name == "run" && descriptor == "()V"
    }
}
```

通过在自定义的`MethodVisitor`中重写`visitInSn()`即可植入自定义代码：

```kotlin
class TrackerMethodAdapter(
    private val descriptor: String?,
    api: Int,
    access: Int,
    mv: MethodVisitor?
) : LocalVariablesSorter(api, access, descriptor, mv) {

    override fun visitInsn(opcode: Int) {
        // 植入代码
        weaveTrackCode()
        super.visitInsn(opcode)
    }

    private fun weaveTrackCode() {
        mv.visitMethodInsn(
            INVOKESTATIC,
            "me/wsj/apm/thread/ThreadTracker",
            "trackOnce",
            "()V",
            false
        )
    }
}
```

植入代码后编译出的.class代码如下：

```java
private void test1() {
	(new Thread(new Runnable() {
		public void run() {
			Log.e("FuncActivity", "thread - new Runnable");
			ThreadTracker.trackOnce("me.wsj.performance.ui.FuncActivity$1 line num: 33");		
        }
	})).start();
}
```

详细代码可以参考：[OutSiderAPM/ThreadTrackerClassAdapter.kt at master · Jinxqq/OutSiderAPM · GitHub](https://github.com/Jinxqq/OutSiderAPM/blob/master/apm-plugin/src/main/kotlin/me/wsj/plugin/internal/bytecode/thread/ThreadTrackerClassAdapter.kt)



##### 4，小结

1，自定义`ClassVisitor`重写`visitMethod()`方法

2，在其中根据方法的`name`及`descriptor`做过滤，找到需要hook的Method

3，自定义`MethodVisitor`重写`visitInSn()`方法

4，在其中插入自定义代码的ASM代码





### 二，Lambda方式

通过Lambda表达式启动线程：

```java
public class FuncActivity {
    
    private void test2(){
        
        new Thread(() -> {
            Log.e("FuncActivity", "thread - lambda");
        }).start();
        
    }
}
```

##### 1，编译后的.class文件

通过Android Studio查看编译后的.class文件，发现编辑器对其做了反编译，看到的还是Lambda的形式：

```java
public class FuncActivity {

    private void test2() {
        (new Thread(() -> {
            Log.e("FuncActivity", "thread - lambda");
        })).start();
    }
}
```

实际上Lambda表达式在编译时会生成一个方法，此方法默认是**隐藏**的，如果想查看，可以使用 java 的 `javap -p -v xxx.class` 命令查看这个方法。也可以通过编译后的ASM代码查看生成的方法。

##### 2，编译后的ASM代码

通过**Byte Code Analyzer**插件查看对应ASM代码如下：

```java
// test2方法部分
{
methodVisitor = classWriter.visitMethod(ACC_PRIVATE, "test2", "()V", null, null);
methodVisitor.visitCode();
...
methodVisitor.visitTypeInsn(NEW, "java/lang/Thread");
methodVisitor.visitInsn(DUP);

// 通过visitInvokeDynamicInsn访问lambda方法
methodVisitor.visitInvokeDynamicInsn("run", "()Ljava/lang/Runnable;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), new Object[]{Type.getType("()V"), new Handle(Opcodes.H_INVOKESTATIC, "me/wsj/performance/ui/FuncActivity", "lambda$test2$0", "()V", false), Type.getType("()V")});

methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Thread", "<init>", "(Ljava/lang/Runnable;)V", false);
...
methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "start", "()V", false);
...
methodVisitor.visitInsn(RETURN);
...
methodVisitor.visitEnd();
}

// Lambda方法部分
{
methodVisitor = classWriter.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, "lambda$test2$0", "()V", null, null);
methodVisitor.visitCode();
...
methodVisitor.visitLdcInsn("FuncActivity");
methodVisitor.visitLdcInsn("thread - lambda");
methodVisitor.visitMethodInsn(INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I", false);
methodVisitor.visitInsn(POP);
...
methodVisitor.visitInsn(RETURN);
methodVisitor.visitMaxs(2, 0);
methodVisitor.visitEnd();
}
```

代码包含两个方法：

**第一个方法**就是我们写的`test2()`，其大致逻辑如下：

1. 通过`NEW`指令创建一个`Thread`对象
2. 通过`INVOKEDYNAMIC`指令创建一个`Runnable`对象
3. 使用这个`Runnable`对象初始化`Thread`对象
4. 执行了`Thread`对象的`start()`方法

**第二个方法**是自动生成的方法，使用了`ACC_SYNTHETIC`来修饰，方法名为`lambda$test2$0`。其中包含了我们写的Lambda表达式中的代码逻辑。

第一个方法通过**InvokeDynamic**指令调用了第二个方法，**InvokeDynamic**指令是在 JDK 7 引入的，用来实现动态类型语言功能，简单来说就是能够在运行时去调用实际的代码。接着重点看一下`methodVisitor.visitInvokeDynamicInsn()`：

```java
// 这行代码很长，格式化如下
methodVisitor.visitInvokeDynamicInsn(
    "run", 
    "()Ljava/lang/Runnable;", 
    new Handle(
        Opcodes.H_INVOKESTATIC, 
        "java/lang/invoke/LambdaMetafactory", 
        "metafactory", 
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", 
        false
    ), 
    new Object[]{
        Type.getType("()V"), 
        new Handle(
            Opcodes.H_INVOKESTATIC, 
            "me/wsj/performance/ui/FuncActivity", 
            // 第二个方法的name
            "lambda$test2$0", 
            "()V", 
            false
        ), 
        Type.getType("()V")
    }
);
```



> MethodType 描述了方法的参数和返回值，MethodHandle 则是根据类名、方法名并且配合 MethodType 来找到特定方法然后执行它；MethodType 和 MethodHandle 配合起来完整表达了一个方法的构成。
>
> 链接：https://www.jianshu.com/p/ec19af9b2f19

其中第四个参数`new Object[]`的第二个参数是一个`MethodHandle `对象，`MethodHandle `对象的第二个参数是Lambda方法所在的类名，三个参数`lambda$test2$0`是Lambda方法的方法名。

那么我们简单总结一下Lambda编译后的逻辑就是：根据Lambda表达式的代码生成一个方法，在调用Lambda表达式的地方调用这个生成的方法。事实上**InvokeDynamic**指令会创建一个对象，对象内部调用了生成的方法，这里不做深究。

下面分两种方式实现对Lambda表达式hook：

##### 3，对Lambda表达式hook（一）

和匿名内部类的方法一样只需要找到Lambda表达式在编译时代码生成方法（如`lambda$test2$0`），在其中插入代码即可。

```kotlin
class LambdaNodeAdapter(api: Int, val classVisitor: ClassVisitor) : ClassNode(api) {
    init {
        this.cv = classVisitor
    }

    override fun visitEnd() {
        super.visitEnd()
        if (TypeUtil.isNeedWeaveMethod(this.name, access)) {
            val shouldHookMethodList = mutableSetOf<String>()
            for (methodNode in this.methods) {
                // 判断方法内部是否有需要处理的 lambda 表达式
                val invokeDynamicInsnNodes = methodNode.findHookPointLambda()
                invokeDynamicInsnNodes.forEach {
                    val handle = it.bsmArgs[1] as? Handle
                    if (handle != null) {
                        shouldHookMethodList.add(handle.name + handle.desc)
                    }
                }
            }
            if (shouldHookMethodList.isNotEmpty()) {
                for (methodNode in methods) {
                    val methodNameWithDesc = methodNode.nameWithDesc
                    if (shouldHookMethodList.contains(methodNameWithDesc)) {
                        // 获取当前方法的指令集
                        val instructions = methodNode.instructions
                        if (instructions != null && instructions.size() > 0) {
                            // 植入代码
                            val list = InsnList()
                            list.add(
                                MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "me/wsj/apm/thread/ThreadTracker",
                                    "trackOnce",
                                    "()V",
                                )
                            )
                            // 将要植入的指令集插入当前方法的指令集
                            instructions.insert(list)
                        }
                    }
                }
            }
        }
        accept(cv)
    }
}
```

1，继承**ClassNode**，可以获取当前类中的所有方法，遍历每个方法中的指令集找到符合条件的Lambda表达式。

2，在Lambda表达式生成的方法中的指令集中插入Hook代码的指令集即可实现hook。

该方法存在一定缺陷：1，难以获取插入位置的行号。2，只能针对Lambda表达式进行Hook，无法对方法引用进行Hook。

详细代码可以参考：[OutSiderAPM/LambdaNodeAdapter.kt at master · Jinxqq/OutSiderAPM · GitHub](https://github.com/Jinxqq/OutSiderAPM/blob/master/apm-plugin/src/main/kotlin/me/wsj/plugin/internal/bytecode/lam/LambdaNodeAdapter.kt)



##### 4，对Lambda表达式hook（二）

方案一存在着两个缺陷，那么能否克服着两个缺陷呢？

可以通过新建一个方法来代理原方法，然后在代理方法中调用原方法的同时也可以植入我们Hook的代码。

具体做法是：生成一个新的方法，新的方法中实现 **InvokeDynamic**指令中描述的代码逻辑。然后创建新的 `MethodHandle`，将这个 `MethodHandle` 替换原本的 `MethodHandle`。代码如下：

```kotlin
class LambdaMethodReferAdapter(api: Int, val classVisitor: ClassVisitor) : ClassNode(api) {
    init {
        this.cv = classVisitor
    }

    private val hookSignature = "run()V"

    private val syntheticMethodList = ArrayList<MethodNode>()

    private val counter = AtomicInteger(0)


    override fun visitEnd() {
        super.visitEnd()
		this.methods.forEach { methodNode ->
			val iterator = methodNode.instructions.iterator()
			while (iterator.hasNext()) {
				val node = iterator.next()
				if (node is InvokeDynamicInsnNode) {
					val desc = node.desc
					val descType = Type.getType(desc)
					val samBaseType = descType.returnType
					// sam 接口名
					val samBase = samBaseType.descriptor
					// sam 方法名
					val samMethodName: String = node.name
					val bsmArgs: Array<Any> = node.bsmArgs
					// sam 方法描述符
					val samMethodType = bsmArgs[0] as Type
					// sam 实现方法实际参数描述符
					val implMethodType = bsmArgs[2] as Type
					// sam name + desc，可以用来辨别是否是需要 Hook 的 lambda 表达式
					val bsmMethodNameAndDescriptor = samMethodName + samMethodType.descriptor
					// 判断是否需要hook
					if (hookSignature != bsmMethodNameAndDescriptor) {
						continue
					}

					// 中间方法的名称
					val middleMethodName =
						"lambda$" + samMethodName + "\$wsj" + counter.incrementAndGet()
					// 中间方法的描述符
					var middleMethodDesc = ""
					val descArgTypes: Array<Type> = descType.argumentTypes
					if (descArgTypes.isEmpty()) {
						middleMethodDesc = implMethodType.descriptor
					} else {
						middleMethodDesc = "("
						for (tmpType in descArgTypes) {
							middleMethodDesc += tmpType.descriptor
						}
						middleMethodDesc += implMethodType.descriptor.replace("(", "")
					}

					// INDY原本的handle，将此handle替换成新的handle
					val oldHandle = bsmArgs[1] as Handle
					val newHandle = Handle(
						Opcodes.H_INVOKESTATIC,
						name, middleMethodName, middleMethodDesc, false
					)
					val newDynamicNode = InvokeDynamicInsnNode(
						node.name,
						node.desc,
						node.bsm,
						samMethodType,
						newHandle,
						implMethodType
					)
					iterator.remove()
					iterator.add(newDynamicNode)
					generateMiddleMethod(oldHandle, middleMethodName, middleMethodDesc)
				}
			}
		}
        
        methods.addAll(syntheticMethodList)
        accept(cv)
    }

    private fun generateMiddleMethod(
        oldHandle: Handle,
        middleMethodName: String,
        middleMethodDesc: String
    ) {
        val methodNode = LambdaMiddleMethodAdapter(this.name,oldHandle, middleMethodName, middleMethodDesc)
        methodNode.visitCode()
        // 添加到中间方法列表
        syntheticMethodList.add(methodNode)
    }
}
```

在中间方法中执行hook逻辑：

```kotlin
class LambdaMiddleMethodAdapter(
    private val className: String,
    val oldHandle: Handle,
    methodName: String,
    val methodDesc: String?,
) : MethodNode( /* latest api = */Opcodes.ASM8,Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC /*| Opcodes.ACC_SYNTHETIC*/,
    methodName, methodDesc, null, null) {

    override fun visitCode() {
        super.visitCode()
        // 此处执行hook逻辑
        weaveHookCode(this)

        // 此块 tag 具体可以参考: [https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.invokedynamic](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.invokedynamic)
        var accResult = oldHandle.tag
        when (accResult) {
            Opcodes.H_INVOKEINTERFACE -> accResult = Opcodes.INVOKEINTERFACE
            Opcodes.H_INVOKESPECIAL -> accResult = Opcodes.INVOKESPECIAL // private, this, super 等会调用
            Opcodes.H_NEWINVOKESPECIAL -> {
                // constructors
                accResult = Opcodes.INVOKESPECIAL
                this.visitTypeInsn(Opcodes.NEW, oldHandle.owner)
                this.visitInsn(Opcodes.DUP)
            }
            Opcodes.H_INVOKESTATIC -> accResult = Opcodes.INVOKESTATIC
            Opcodes.H_INVOKEVIRTUAL -> accResult = Opcodes.INVOKEVIRTUAL
        }
        val middleMethodType = Type.getType(methodDesc)
        val argumentsType = middleMethodType.argumentTypes
        if (argumentsType.isNotEmpty()) {
            var loadIndex = 0
            for (tmpType in argumentsType) {
                val opcode = tmpType.getOpcode(Opcodes.ILOAD)
                this.visitVarInsn(opcode, loadIndex)
                loadIndex += tmpType.size
            }
        }
        this.visitMethodInsn(
            accResult,
            oldHandle.owner,
            oldHandle.name,
            oldHandle.desc,
            false
        )
        val returnType = middleMethodType.returnType
        val returnOpcodes = returnType.getOpcode(Opcodes.IRETURN)
        this.visitInsn(returnOpcodes)
        this.visitEnd()
    }


    private fun weaveHookCode(mv: MethodVisitor) {
		// todo 植入代码
    }
}
```

最终编译出来的.class如下：

```java
public class FuncActivity {

	private void test2() {
        (new Thread(FuncActivity::lambda$run$wsj1)).start();
    }

    private static void lambda$run$wsj1() {
        ThreadTracker.trackOnce("me/wsj/performance/ui/FuncActivity by Lambda:");
        lambda$test2$0();
    }
    
}
```

详细代码可以参考：[OutSiderAPM/LambdaMethodReferAdapter.kt at master · Jinxqq/OutSiderAPM · GitHub](https://github.com/Jinxqq/OutSiderAPM/blob/master/apm-plugin/src/main/kotlin/me/wsj/plugin/internal/bytecode/lam/LambdaMethodReferAdapter.kt)



### 三，方法引用

有些时候我们可以通过方法引用来简化Lambda表达式，如下：

```java
public class FuncActivity {
	private void test3(){
        new Thread(MyThreadPool::getInstance).start();
    }
}
```

直接看其编译后的ASM如下：

```java
{
methodVisitor = classWriter.visitMethod(ACC_PRIVATE, "test3", "()V", null, null);
methodVisitor.visitCode();
...
methodVisitor.visitTypeInsn(NEW, "java/lang/Thread");
methodVisitor.visitInsn(DUP);
    
methodVisitor.visitInvokeDynamicInsn("run", "()Ljava/lang/Runnable;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), new Object[]{Type.getType("()V"), new Handle(Opcodes.H_INVOKESTATIC, "me/wsj/performance/test/CibThreadPool", "getInstance", "()Lme/wsj/performance/test/CibThreadPool;", false), Type.getType("()V")});
    
methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Thread", "<init>", "(Ljava/lang/Runnable;)V", false);
methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "start", "()V", false);
...
methodVisitor.visitInsn(RETURN);
...
methodVisitor.visitEnd();
}
```

可见跟Lambda一样都是调用了**InvokeDynamic**指令，`methodVisitor.visitInvokeDynamicInsn()`如下：

```java
methodVisitor.visitInvokeDynamicInsn(
    "run", 
    "()Ljava/lang/Runnable;", 
    new Handle(
        Opcodes.H_INVOKESTATIC, 
        "java/lang/invoke/LambdaMetafactory", 
        "metafactory", 
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", 
        false), 
    new Object[]{
        Type.getType("()V"), 
        new Handle(
            Opcodes.H_INVOKESTATIC, 
            "me/wsj/performance/test/CibThreadPool", 
            "getInstance", 
            "()Lme/wsj/performance/test/CibThreadPool;", 
            false), 
        Type.getType("()V")
    }
);
```

跟Lambda不同的是，这里没有生成新的方法，因为**方法引用**是一个现成的方法，可以直接访问。因此对**方法引用**进行hook的思路和对Lambda表达式hook一样，都是生成一个方法，方法内调用源方法，最后替换调用处。

使用同Lambda同样的方式（方案二），最终结果如下：

```java
public class FuncActivity {

	private void test3() {
        (new Thread(FuncActivity::lambda$run$wsj2)).start();
    }

    private static void lambda$run$wsj2() {
        ThreadTracker.trackOnce("me/wsj/performance/ui/FuncActivity by MethodReference:");
        CibThreadPool.getInstance();
    }
    
}
```



### 四，总结

1. 匿名内部类的方式只需要根据方法名及方法签名作为hook点，植入代码即可。

2. Lambda表达式方式的Hook有两种方案：

   2.1，找到Lambda表达式编译生成的方法，在其指令集中植入Hook的代码指令集即可实现Hook。

   2.2，生成一个中间方法，在这个方法中调用这个 Lambda 编译时生成的中间方法，然后将自定义的 MethodHandle 指向生成的方法，最后替换掉Bootstrap Mehtod中的MethodHandle，达到偷梁换柱的效果。

3. 方法引用的方式的思路是：生成一个中间方法，把**方法引用**里的内容放到生成的中间方法中，然后将自定义的 MethodHandle 指向生成的方法，最后替换掉 Bootstrap Method 中的 MethodHandle，达到偷梁换柱的效果。



参考：

https://www.jianshu.com/p/ec19af9b2f19

https://juejin.cn/post/7042328862872567838#heading-3