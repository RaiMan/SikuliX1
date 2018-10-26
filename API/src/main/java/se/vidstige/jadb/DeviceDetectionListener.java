package se.vidstige.jadb;

import java.util.List;

public interface DeviceDetectionListener {
    void onDetect(List<JadbDevice> devices);
    void onException(Exception e);
}

