package io.sympli.find_e.event;

import java.util.Arrays;
import java.util.HashSet;

public class PermissionsGrantResultEvent {

    public boolean permissionsGranted;
    public HashSet<String> permissions;

    public PermissionsGrantResultEvent(boolean permissionsGranted, String... permissions) {
        this.permissionsGranted = permissionsGranted;
        this.permissions = new HashSet<>(Arrays.asList(permissions));
    }
}
