package com.loopme.config.provider.source;

import com.loopme.config.api.Configuration;

public interface Listener {

    void onUpdate(Configuration fresh);

}
