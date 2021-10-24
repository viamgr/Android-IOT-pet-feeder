package com.part.livetaskcore.views

import com.part.livetaskcore.Resource
import com.part.livetaskcore.livatask.LiveTask

// TODO: 10/24/2021 handle default error text
fun Resource.Error.getErrorText(liveTask: LiveTask<*>) =
    liveTask.errorMapper().mapError(this.exception).message