/**
 * OWASP Benchmark Project
 *
 * <p>This file is part of the Open Web Application Security Project (OWASP) Benchmark Project For
 * details, please see <a
 * href="https://owasp.org/www-project-benchmark/">https://owasp.org/www-project-benchmark/</a>.
 *
 * <p>The OWASP Benchmark is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, version 2.
 *
 * <p>The OWASP Benchmark is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details
 *
 * @author Dave Wichers
 * @created 2015
 */
package org.owasp.benchmarkutils.score.parsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.owasp.benchmarkutils.score.BenchmarkScore;
import org.owasp.benchmarkutils.score.ResultFile;
import org.owasp.benchmarkutils.score.TestSuiteResults;
import org.owasp.benchmarkutils.score.parsers.csv.SemgrepCSVReader;
import org.owasp.benchmarkutils.score.parsers.csv.WhiteHatDynamicReader;
import org.owasp.benchmarkutils.score.parsers.sarif.CodeQLReader;
import org.owasp.benchmarkutils.score.parsers.sarif.ContrastScanReader;
import org.owasp.benchmarkutils.score.parsers.sarif.DatadogSastReader;
import org.owasp.benchmarkutils.score.parsers.sarif.FortifySarifReader;
import org.owasp.benchmarkutils.score.parsers.sarif.PTAIReader;
import org.owasp.benchmarkutils.score.parsers.sarif.PrecautionReader;
import org.owasp.benchmarkutils.score.parsers.sarif.SemgrepSarifReader;
import org.owasp.benchmarkutils.score.parsers.sarif.SnykReader;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class Reader {

    protected final ObjectMapper jsonMapper = new ObjectMapper();
    protected final XmlMapper xmlMapper = new XmlMapper();

    // TODO: Figure out how to dynamically add all readers without listing them manually
    // NOTE: There is a unit test that at least automatically verifies that any reader with a unit
    // test is in this list
    public static List<Reader> allReaders() {
        return Arrays.asList(
                new AcunetixReader(),
                new AppScanDynamicReader(),
                new AppScanSourceReader(),
                new ArachniReader(),
                new BearerReader(),
                new BlackDuckReader(),
                new BurpJsonReader(),
                new BurpReader(),
                new CASTAIPReader(),
                new CheckmarxESReader(),
                new CheckmarxIASTReader(),
                new CheckmarxReader(),
                new CodeQLReader(),
                new ContrastAssessReader(),
                new ContrastScanReader(),
                new CoverityReader(),
                new CrashtestReader(),
                new DatadogReader(),
                new DatadogSastReader(),
                new FaastReader(),
                new FindbugsReader(),
                new FluidAttacksReader(),
                new FortifyReader(),
                new FortifySarifReader(),
                new FusionLiteInsightReader(),
                new HCLAppScanIASTReader(),
                new HCLAppScanSourceReader(),
                new HCLAppScanStandardReader(),
                new HorusecReader(),
                new InsiderReader(),
                new JuliaReader(),
                new KlocworkCSVReader(),
                new KiuwanReader(),
                new MendReader(),
                new NetsparkerReader(),
                new NJSScanReader(),
                new NoisyCricketReader(),
                new ParasoftReader(),
                new PrecautionReader(),
                new PMDReader(),
                new PTAIReader(),
                new QualysWASReader(),
                new Rapid7Reader(),
                new ReshiftReader(),
                new ScnrReader(),
                new SeekerReader(),
                new SemgrepReader(),
                new SemgrepCSVReader(),
                new SemgrepSarifReader(),
                new ShiftLeftReader(),
                new ShiftLeftScanReader(),
                new SnappyTickReader(),
                new SnykReader(),
                new SonarQubeJsonReader(),
                new SonarQubeReader(),
                new SourceMeterReader(),
                new ThunderScanReader(),
                new VeracodeReader(),
                new VisualCodeGrepperReader(),
                new W3AFReader(),
                new WapitiJsonReader(),
                new WapitiReader(),
                new WebInspectReader(),
                new WhiteHatDynamicReader(),
                new ZapJsonReader(),
                new ZapReader());
    }

    public abstract boolean canRead(ResultFile resultFile);

    public abstract TestSuiteResults parse(ResultFile resultFile) throws Exception;

    public static Node getNamedNode(String name, NodeList list) {
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);

            if (n.getNodeName().equals(name)) {
                return n;
            }
        }

        return null;
    }

    // Returns the node inside this nodelist whose name matches 'name', that also
    // has an attribute called 'key' whose value matches 'keyvalue'

    public static Node getNamedNode(String name, String keyValue, NodeList list) {
        if ((name == null) || (keyValue == null) || (list == null)) return null;
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n.getNodeName().equals(name)) {
                if (keyValue.equals(getAttributeValue("key", n))) {
                    return n;
                }
            }
        }
        return null;
    }

    public static Node getNamedChild(String name, Node parent) {
        NodeList children = parent.getChildNodes();
        return getNamedNode(name, children);
    }

    public static boolean hasNamedChild(String name, Node parent) {
        NodeList children = parent.getChildNodes();
        return getNamedNode(name, children) != null;
    }

    public static List<Node> getNamedChildren(String name, List<Node> list) {
        List<Node> results = new ArrayList<>();
        for (Node n : list) {
            NodeList children = n.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equals(name)) {
                    results.add(child);
                }
            }
        }
        return results;
    }

    public static List<Node> getNamedChildren(String name, Node parent) {
        NodeList children = parent.getChildNodes();
        return getNamedNodes(name, children);
    }

    public static List<Node> getNamedNodes(String name, NodeList list) {
        List<Node> results = new ArrayList<Node>();
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n.getNodeName().equals(name)) {
                // System.out.println(">> " + n.getNodeName() + "::" + n.getNodeValue());
                results.add(n);
            }
        }
        return results;
    }

    public static String getAttributeValue(String name, Node node) {
        if (node == null) return null;
        NamedNodeMap nnm = node.getAttributes();
        if (nnm != null) {
            Node attrnode = nnm.getNamedItem(name);
            if (attrnode != null) {
                return attrnode.getNodeValue();
            }
        }
        return null;
    }

    private static String manipulateTestcase(String path) {
        if (path.startsWith(BenchmarkScore.TESTCASENAME)) {
            int latest = path.indexOf(".");
            String toReplace = path.substring(0, latest);
            path = path.replaceAll(toReplace, BenchmarkScore.TESTCASENAME);
            System.out.println(path);
        }
        return path;
    }

    private static int findFirstNonNumeric(String path) {
        for (int i = 0; i < path.length(); i++) {
            if (!Character.isDigit(path.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    public static long occurrences(String path, char c) {
        return path.chars().filter(ch -> ch == c).count();
    }

    public static int testNumber(String path) {
        return testNumber(path, BenchmarkScore.TESTCASENAME);
    }

    /** Get rid of everything except the test name. */
    public static int testNumber(String path, String testCaseName) {
        try {
            // No BenchmarkTest
            if (!path.contains(testCaseName)) {
                return -1;
            }
            int numberStart = path.indexOf(testCaseName) + testCaseName.length() + 1;
            path = path.substring(numberStart);
            // System.out.println("After length: " + path);
            path = path.replaceAll("\\?.*", "");
            path = path.replaceAll(",.*", "");

            path = path.replaceAll(testCaseName + "v[0-9]*", testCaseName);

            path = path.replaceAll("/send", "");
            if (path.contains(":")) {
                path = removeColon(path);
            }
            path = path.replaceAll("[^0-9.]", "");
            // System.out.println("After replace: " + path);
            if (path.contains(".") && occurrences(path, '.') > 1) {
                int start = path.indexOf(".") + 1;
                int end = path.length();
                if (end - start > 1) {
                    path = path.substring(start, end);
                }
            }
            if (path.contains(".")) {
                path = removeFileEnding(path);
            }
            // System.out.println("Before dot cleaning " + path);

            // Remove remaining dots
            path = path.replace(".", "");
            // System.out.println("Final: " + path);

            // In the case of $innerclass
            int dollar = path.indexOf("$");
            if (dollar != -1) {
                path = path.substring(0, dollar);
            }
            return Integer.parseInt(path);

        } catch (Exception e) {

            return -1;
        }
    }

    public static String extractFilenameWithoutEnding(String path) {
        try {
            String name = new File(fixWindowsPath(removeUrlPart(path))).getName();

            if (name.contains(".")) {
                return name.substring(0, name.lastIndexOf("."));
            } else {
                return name;
            }
        } catch (Throwable t) {
            return "";
        }
    }

    private static String removeColon(String filename) {
        return filename.substring(0, filename.lastIndexOf(':'));
    }

    private static String removeFileEnding(String filename) {
        return filename.substring(0, filename.lastIndexOf('.'));
    }

    private static String fixWindowsPath(String path) {
        return path.replace("\\", File.separator);
    }

    private static String removeUrlPart(String path) throws MalformedURLException {
        if (path.startsWith("http")) {
            path = new URL(path).getPath();
        }
        return path;
    }
}
