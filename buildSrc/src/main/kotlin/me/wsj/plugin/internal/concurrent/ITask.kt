package me.wsj.plugin.internal.concurrent

import java.util.concurrent.Callable

interface ITask : Callable<Any>