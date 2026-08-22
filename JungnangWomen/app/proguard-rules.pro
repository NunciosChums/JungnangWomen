# 스택 트레이스 분석을 위해 소스 파일명/라인 번호 유지
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Retrofit: 제네릭 시그니처 및 어노테이션 유지 (suspend 함수, 응답 타입 파싱에 필요)
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**

# kotlinx.serialization: @Serializable 모델의 serializer 유지
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class kr.susemi99.jungnangwomen.**$$serializer { *; }
-keepclassmembers class kr.susemi99.jungnangwomen.** { *** Companion; }
-keepclasseswithmembers class kr.susemi99.jungnangwomen.** { kotlinx.serialization.KSerializer serializer(...); }
