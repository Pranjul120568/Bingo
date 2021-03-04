package com.pdinc.bingo.Modals

data class User(
        val nickName:String,
        val avatarUrl:String,
        val uId:String
){
    constructor():this("","","")
}