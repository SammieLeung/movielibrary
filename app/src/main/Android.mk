LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

#libname这个名字可以自己定。注意这里的修改！！加了libname2   
LOCAL_STATIC_JAVA_LIBRARIES :=fastjson glide-3.7.0 guava-19.0 jsoup-1.8.3 okhttp-3.4.1 okio-1.10.0 httpclient-4.5.3 httpcore-4.4.6 lang3-3.6 \
             py4j \
	     qwertysearch \
   	     android-support-v7-recyclerview \
    	     android-support-v7-appcompat \
    	     android-support-v13 \
    		 android-support-design \
    		 umeng-analytics-v6.12 \
    		 utdid4all-1.0.4
				

LOCAL_SRC_FILES := $(call all-java-files-under,java)


LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res \
   frameworks/support/v7/appcompat/res \
   frameworks/support/v7/recyclerview/res \
     frameworks/support/design/res

LOCAL_PACKAGE_NAME := MovieLibrary
LOCAL_CERTIFICATE := platform
LOCAL_MODULE_TAGS := optional
LOCAL_AAPT_FLAGS := --auto-add-overlay \
    --extra-packages android.support.v7.appcompat:android.support.v7.recyclerview\
    --extra-packages android.support.design

include $(BUILD_PACKAGE)

########################################
include $(CLEAR_VARS)
#libname必须与上面自己定义的名称一致,needimport.jar是你需要导入的第三方jar包.注意这里的修改！！   #加了libname2:lib/needimport2.jar  
MY_AS_LIBS:=../../libs

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := fastjson:$(MY_AS_LIBS)/fastjson-1.2.31.jar \
    glide-3.7.0:$(MY_AS_LIBS)/glide-3.7.0.jar \
    guava-19.0:$(MY_AS_LIBS)/guava-19.0.jar \
    jsoup-1.8.3:$(MY_AS_LIBS)/jsoup-1.8.3.jar \
    okhttp-3.4.1:$(MY_AS_LIBS)/okhttp-3.4.1.jar \
    okio-1.10.0:$(MY_AS_LIBS)/okio-1.10.0.jar \
    httpclient-4.5.3:$(MY_AS_LIBS)/httpclient-4.5.3.jar \
    httpcore-4.4.6:$(MY_AS_LIBS)/httpcore-4.4.6.jar \
    lang3-3.6:$(MY_AS_LIBS)/commons-lang3-3.6.jar \
    py4j:$(MY_AS_LIBS)/pinyin4j.jar \
    qwertysearch:$(MY_AS_LIBS)/qwertysearch.jar \
    umeng-analytics-v6.12:$(MY_AS_LIBS)/umeng/umeng-analytics-v6.1.2.jar \
    utdid4all-1.0.4:$(MY_AS_LIBS)/umeng/utdid4all-1.0.4.jar \
   
#LOCAL_PREBUILT_LIBS := ../externalLibrary/vitamio/libs/armeabi/libtchip-vinit.so    

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
