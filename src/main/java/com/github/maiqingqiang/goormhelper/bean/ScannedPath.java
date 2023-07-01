package com.github.maiqingqiang.goormhelper.bean;

import java.util.ArrayList;
import java.util.List;

@lombok.NoArgsConstructor
@lombok.Data
public class ScannedPath {
    private List<String> schema = new ArrayList<>();
    private long lastModified;
}
