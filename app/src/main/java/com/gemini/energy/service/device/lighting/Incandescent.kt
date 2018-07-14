package com.gemini.energy.service.device.lighting

import com.gemini.energy.service.IComputable
import com.gemini.energy.service.OutgoingRows

class Incandescent : IComputable {

    override fun compute(): List<List<OutgoingRows>> {
        return listOf()
    }

}