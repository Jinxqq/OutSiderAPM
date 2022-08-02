package me.wsj.plugin.internal.bytecode.webview

import me.wsj.plugin.internal.MethodDetail
import me.wsj.plugin.utils.TypeUtil
import me.wsj.plugin.utils.log
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

class WebMethodAdapter(
    methodName: String,
    methodDesc: String?,
    api: Int,
    access: Int,
    mv: MethodVisitor?
) : AdviceAdapter(api, mv, access, methodName, methodDesc) {

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        // webView.setWebViewClient()
        // ...
        if (TypeUtil.isSetWebViewClient(name, descriptor)) {
//            System.out.println(className + " - " + owner + " - " + name + " - " + descriptor + " - " + opcode);
            // webViewClient webView ...
            mv.visitInsn(DUP2)
            // webViewClient webView webViewClient webView ...
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            // webViewClient webView ...
            mv.visitInsn(POP)
            // webView
            mv.visitTypeInsn(NEW, "me/wsj/core/job/webview/JSBridge")
            mv.visitInsn(DUP)
            mv.visitMethodInsn(INVOKESPECIAL, "me/wsj/core/job/webview/JSBridge", "<init>", "()V", false)
            mv.visitLdcInsn("android_apm")
            mv.visitMethodInsn(INVOKEVIRTUAL, "android/webkit/WebView", "addJavascriptInterface", "(Ljava/lang/Object;Ljava/lang/String;)V", false)
        } else {
//            System.out.println("- owner: " + owner + "  name: " + name);
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }
    }

    override fun visitCode() {
        // onPageFinished(){ ... }
        if (TypeUtil.isOnPageFinishedMethod(name, methodDesc)){
            mv.visitVarInsn(ALOAD, 1)
            mv.visitMethodInsn(INVOKEVIRTUAL, "android/webkit/WebView", "getProgress", "()I", false)
            mv.visitIntInsn(BIPUSH, 100)
            val l0 = Label()
            mv.visitJumpInsn(IF_ICMPNE, l0)
            mv.visitVarInsn(ALOAD, 1)
            mv.visitMethodInsn(INVOKEVIRTUAL,"android/webkit/WebView","getSettings","()Landroid/webkit/WebSettings;",false)
            mv.visitVarInsn(ASTORE, 3)
            mv.visitVarInsn(ALOAD, 3)
            mv.visitInsn(ICONST_1)
            mv.visitMethodInsn(INVOKEVIRTUAL, "android/webkit/WebSettings", "setJavaScriptEnabled", "(Z)V", false)
    //        mv.visitVarInsn(ALOAD, 1)
    //        mv.visitTypeInsn(NEW, "me/wsj/core/job/webview/JSBridge")
    //        mv.visitInsn(DUP)
    ////        mv.visitVarInsn(ALOAD, 2)
    //        mv.visitMethodInsn(INVOKESPECIAL, "me/wsj/core/job/webview/JSBridge", "<init>", "()V", false)
    //        mv.visitLdcInsn("android_apm")
    //        mv.visitMethodInsn(INVOKEVIRTUAL, "android/webkit/WebView", "addJavascriptInterface", "(Ljava/lang/Object;Ljava/lang/String;)V", false)
            mv.visitLdcInsn("javascript:%s.sendResource(\"%s\", JSON.stringify(window.performance.timing));")
            mv.visitInsn(ICONST_2)
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object")
            mv.visitInsn(DUP)
            mv.visitInsn(ICONST_0)

            mv.visitLdcInsn("android_apm");
            mv.visitInsn(AASTORE);
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "format", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", false)
            mv.visitVarInsn(ASTORE, 4)
            mv.visitVarInsn(ALOAD, 1)
            mv.visitVarInsn(ALOAD, 4)
            mv.visitMethodInsn(INVOKEVIRTUAL, "android/webkit/WebView", "loadUrl", "(Ljava/lang/String;)V", false)
            mv.visitLabel(l0)
        }
        super.visitCode()
    }
}