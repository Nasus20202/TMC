package dev.nasuta.tmc.lab3;

import java.util.List;
import java.util.Map;

public record WayData(long id, List<Long> nodeRefs, Map<String, String> tags) {
}