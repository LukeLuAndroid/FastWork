# -*-coding:utf-8-*-
# python调用java

import jpype
from jpype import *
import os.path

inputFile = os.path.join(os.path.abspath('.'), r'input.txt')

mappingFile = os.path.join(os.path.abspath('.'), r'mapping.txt')

jar_path = os.path.join(os.path.abspath('.'), r'retrace.jar')
print(jar_path)

jvm_path = jpype.getDefaultJVMPath()
print(jvm_path)


# ext_path = os.path.join(os.path.abspath('.'), r'exts')

jpype.startJVM(jvm_path, '-ea', '-Djava.class.path=%s' % jar_path)

JClass = jpype.JClass('proguard.retrace.ReTrace')

inputs = '''Caused by: java.lang.RuntimeException: this is a test
        at com.app.config.d.a(Unknown Source)
        at com.app.config.MainActivity.onCreate(Unknown Source)
        at android.app.Activity.performCreate(Activity.java:6682)
        at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1118)
'''

File = jpype.JClass("java.io.File")
Boolean = jpype.JClass("java.lang.Boolean")
instance = JClass(
    "(?:\\s*%c:.*)|(?:\\s*at\\s+%c.%m\\s*\\(.*?(?::%l)?\\)\\s*)", Boolean.TRUE.booleanValue(), File(mappingFile))

System = jpype.JClass('java.lang.System')
PrintStream = jpype.JClass('java.io.PrintStream')
oldOut = System.out
out = PrintStream("test.txt")
System.setOut(out)

ByteArrayInputStream = jpype.JClass("java.io.ByteArrayInputStream")
in_stream = ByteArrayInputStream(bytes(inputs, encoding='utf8'))
System.setIn(in_stream)

result = instance.execute()

print("result=%s" % result)

# System.out.println("hahahah")
out.flush()

System.setOut(oldOut)
# System.out.print("hahahah")

jpype.shutdownJVM()  # 关闭虚拟机
