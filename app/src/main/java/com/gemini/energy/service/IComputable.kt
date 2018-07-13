package com.gemini.energy.service

interface IComputable {
    fun compute(): List<List<OutgoingRows>>
}