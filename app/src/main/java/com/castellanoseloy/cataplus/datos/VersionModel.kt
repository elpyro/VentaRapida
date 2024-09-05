package com.castellanoseloy.cataplus.datos

data  class VersionModel (
    var versionName:String? = null,
    var descripcion:String? = null,
    var cancelable:Boolean?=null,
    var versionCode:Int? =null,
)