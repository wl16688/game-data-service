package com.game.tools;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GeoNamesToSql {

    // 配置目标国家 (ISO-3166 2位代码)
    private static final Set<String> TARGET_COUNTRIES = new HashSet<>(Arrays.asList("CN", "US", "JP", "KR", "SG"));

    // 文件下载路径
    private static final String WORK_DIR = "./data";
    private static final String ALL_COUNTRIES_URL = "https://download.geonames.org/export/dump/allCountries.zip";
    private static final String HIERARCHY_URL = "https://download.geonames.org/export/dump/hierarchy.zip";
    private static final String ALT_NAMES_URL = "https://download.geonames.org/export/dump/alternateNamesV2.zip";
    private static final String COUNTRY_INFO_URL = "https://download.geonames.org/export/dump/countryInfo.txt";

    // 内存数据结构
    private static final Map<Integer, Node> nodes = new HashMap<>(); // geonameId -> Node
    private static final Map<Integer, Integer> hierarchy = new HashMap<>(); // childId -> parentId
    private static final Map<Integer, String> zhNames = new HashMap<>(); // geonameId -> zhName
    private static final Map<String, Integer> countryRoots = new HashMap<>(); // countryCode -> geonameId

    static class Node {
        int id;
        String name;
        String countryCode;
        String featureCode;
        int level; // 1:Country, 2:ADM1, 3:ADM2, 4:ADM3, 5:ADM4

        Node(int id, String name, String countryCode, String featureCode) {
            this.id = id;
            this.name = name;
            this.countryCode = countryCode;
            this.featureCode = featureCode;
            this.level = parseLevel(featureCode);
        }

        private int parseLevel(String fcode) {
            if (fcode.startsWith("PCL")) return 1; // Country
            if (fcode.equals("ADM1")) return 2;
            if (fcode.equals("ADM2")) return 3;
            if (fcode.equals("ADM3")) return 4;
            if (fcode.equals("ADM4")) return 5;
            return 99;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting GeoNames ADM4 SQL Generator...");
        Files.createDirectories(Paths.get(WORK_DIR));

        // 1. 下载并解压必要文件
        downloadAndUnzip(COUNTRY_INFO_URL, "countryInfo.txt");
        downloadAndUnzip(ALL_COUNTRIES_URL, "allCountries.txt");
        downloadAndUnzip(HIERARCHY_URL, "hierarchy.txt");
        downloadAndUnzip(ALT_NAMES_URL, "alternateNamesV2.txt");

        // 2. 解析国家根节点
        parseCountryInfo();

        // 3. 解析多语言 (抽取中文名)
        parseAlternateNames();

        // 4. 解析全量地理数据 (提取 ADM1-ADM4)
        parseAllCountries();

        // 5. 解析层级关系 (关联 Parent)
        parseHierarchy();

        // 6. 生成 SQL
        generateSql();
        
        System.out.println("Done! SQL file generated at: " + WORK_DIR + "/regions_full.sql");
    }

    private static void parseCountryInfo() throws IOException {
        System.out.println("Parsing countryInfo.txt...");
        try (BufferedReader br = new BufferedReader(new FileReader(WORK_DIR + "/countryInfo.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) continue;
                String[] cols = line.split("\t");
                if (cols.length > 16) {
                    String iso = cols[0];
                    if (TARGET_COUNTRIES.contains(iso)) {
                        int geonameId = Integer.parseInt(cols[16]);
                        String name = cols[4];
                        countryRoots.put(iso, geonameId);
                        nodes.put(geonameId, new Node(geonameId, name, iso, "PCLI"));
                    }
                }
            }
        }
    }

    private static void parseAlternateNames() throws IOException {
        System.out.println("Parsing alternateNamesV2.txt (Extracting ZH names)...");
        try (BufferedReader br = new BufferedReader(new FileReader(WORK_DIR + "/alternateNamesV2.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = line.split("\t");
                if (cols.length > 3) {
                    int geonameId = Integer.parseInt(cols[1]);
                    String lang = cols[2];
                    String altName = cols[3];
                    // 只要中文，且覆盖已有（GeoNames 中通常排在后面的更短更常用）
                    if ("zh".equals(lang) || "zh-CN".equals(lang)) {
                        zhNames.put(geonameId, altName);
                    }
                }
            }
        }
    }

    private static void parseAllCountries() throws IOException {
        System.out.println("Parsing allCountries.txt (Filtering ADM1-ADM4 for target countries)...");
        try (BufferedReader br = new BufferedReader(new FileReader(WORK_DIR + "/allCountries.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = line.split("\t");
                if (cols.length > 8) {
                    String featureClass = cols[6];
                    String featureCode = cols[7];
                    String countryCode = cols[8];

                    if (!TARGET_COUNTRIES.contains(countryCode)) continue;
                    if (!"A".equals(featureClass)) continue; // 只保留行政区
                    if (!featureCode.startsWith("ADM") || featureCode.length() > 4) continue; // ADM1-ADM4

                    int geonameId = Integer.parseInt(cols[0]);
                    String name = cols[2]; // asciiname
                    if (name == null || name.isEmpty()) name = cols[1];

                    nodes.put(geonameId, new Node(geonameId, name, countryCode, featureCode));
                }
            }
        }
        System.out.println("Filtered " + nodes.size() + " administrative nodes.");
    }

    private static void parseHierarchy() throws IOException {
        System.out.println("Parsing hierarchy.txt (Building parent-child relations)...");
        try (BufferedReader br = new BufferedReader(new FileReader(WORK_DIR + "/hierarchy.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = line.split("\t");
                if (cols.length >= 2) {
                    int parentId = Integer.parseInt(cols[0]);
                    int childId = Integer.parseInt(cols[1]);
                    // 如果 child 是我们关心的节点，并且 parent 也是，则建立关联
                    if (nodes.containsKey(childId) && nodes.containsKey(parentId)) {
                        // 简单的防冲突策略：保留 level 差距最小的父节点
                        Node child = nodes.get(childId);
                        Node candidateParent = nodes.get(parentId);
                        if (!hierarchy.containsKey(childId)) {
                            hierarchy.put(childId, parentId);
                        } else {
                            Node existingParent = nodes.get(hierarchy.get(childId));
                            if (candidateParent.level > existingParent.level && candidateParent.level < child.level) {
                                hierarchy.put(childId, parentId);
                            }
                        }
                    }
                }
            }
        }

        // 兜底策略：没有在 hierarchy 中找到父亲的 ADM1，挂到对应的 Country Root 上
        for (Node node : nodes.values()) {
            if (node.level == 2 && !hierarchy.containsKey(node.id)) {
                Integer rootId = countryRoots.get(node.countryCode);
                if (rootId != null) {
                    hierarchy.put(node.id, rootId);
                }
            }
        }
    }

    private static void generateSql() throws IOException {
        System.out.println("Generating regions_full.sql...");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(WORK_DIR + "/regions_full.sql", StandardCharsets.UTF_8))) {
            bw.write("-- GeoNames Administrative Regions (Level 1-5: Country/ADM1/ADM2/ADM3/ADM4)\n");
            bw.write("-- Target Countries: " + TARGET_COUNTRIES + "\n");
            bw.write("TRUNCATE TABLE regions;\n");
            bw.write("INSERT INTO regions (id, parent_id, name, level) VALUES\n");

            boolean first = true;
            for (Node node : nodes.values()) {
                // 获取上级ID
                int parentId = hierarchy.getOrDefault(node.id, 0);
                if (node.level == 1) parentId = 0; // Country has no parent

                // 获取名称（优先使用中文名）
                String finalName = zhNames.getOrDefault(node.id, node.name);
                // SQL 转义单引号
                finalName = finalName.replace("'", "''");

                if (!first) {
                    bw.write(",\n");
                }
                bw.write(String.format("(%d, %d, '%s', %d)", node.id, parentId, finalName, node.level));
                first = false;
            }
            bw.write(";\n");
        }
    }

    private static void downloadAndUnzip(String urlStr, String targetTxtFile) throws Exception {
        Path targetPath = Paths.get(WORK_DIR, targetTxtFile);
        if (Files.exists(targetPath)) {
            System.out.println("File already exists, skipping download: " + targetTxtFile);
            return;
        }

        System.out.println("Downloading " + urlStr + "...");
        Path zipPath = Paths.get(WORK_DIR, targetTxtFile + ".zip");
        
        // countryInfo.txt 是直接文本下载，不是 zip
        if (urlStr.endsWith(".txt")) {
            try (InputStream in = new URL(urlStr).openStream()) {
                Files.copy(in, targetPath);
            }
            return;
        }

        // 下载 ZIP
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, zipPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        // 解压 ZIP 中的目标 TXT 文件
        System.out.println("Unzipping " + targetTxtFile + "...");
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(targetTxtFile)) {
                    try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
                        byte[] buffer = new byte[1024 * 1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    break;
                }
            }
        }
        Files.deleteIfExists(zipPath); // 删除临时 zip
    }
}
