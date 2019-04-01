import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class API {
    private static final String HOST = "http://pro.boost.com";
    private static final Pattern QUERY_PARAMS_REGEX = Pattern.compile("[\\\\?&]([^&=]+)=([^&=]+)");
    private static final Pattern URL_PARSE_REGEX = Pattern.compile("^(http[s]?:\\/\\/[^:\\/\\s]+)((\\/[\\w\\-\\.]+)*)(\\?.*)?$");

    public static String GET(String query) {
        try {
            String path = checkAccess(query);
            HashMap<String, String> queryParams = getQueryParams(query);
            JSONArray instancesArray = (JSONArray) new JSONParser().parse(readFile(path));
            JSONArray filteredInstancesArray = new JSONArray();
            Iterator instancesItr = instancesArray.iterator();

            while (instancesItr.hasNext()) {
                JSONObject instance = (JSONObject) instancesItr.next();
                boolean toBeReturned = true;

                for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                    if (instance.containsKey(entry.getKey()) && !instance.get(entry.getKey()).equals(entry.getValue())) {
                        toBeReturned = false;
                    }
                }

                if (toBeReturned) {
                    filteredInstancesArray.add(instance);
                }
            }
            return filteredInstancesArray.toJSONString();
        } catch (Exception e) {
            return "500: " + e.getMessage();
        }
    }

    public static String POST(String query, String data) {
        try {
            String path = checkAccess(query);
            HashMap<String, String> queryParams = getQueryParams(query);
            JSONArray instancesArray = (JSONArray) new JSONParser().parse(readFile(path));
            Iterator instancesItr = instancesArray.iterator();

            try {
                JSONObject updateFields = (JSONObject) new JSONParser().parse(data);
                updateFields.remove("id");

                while (instancesItr.hasNext()) {
                    JSONObject instance = (JSONObject) instancesItr.next();
                    boolean toBeUpdated = true;

                    for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                        if (instance.containsKey(entry.getKey()) && !instance.get(entry.getKey()).equals(entry.getValue())) {
                            toBeUpdated = false;
                        }
                    }

                    if (toBeUpdated) {
                        for(Object key : updateFields.keySet())
                        {
                            instance.put(key, updateFields.get(key));
                        }
                    }
                }
            } catch (ParseException e) {
                throw new Error("400: Invalid data");
            }
            writeFile(path, instancesArray.toJSONString());
            return "200: OK";
        } catch (Exception e) {
            return "500: " + e.getMessage();
        }
    }

    public static String PUT(String query, String data) {
        try {
            String path = checkAccess(query);
            JSONArray instancesArray = (JSONArray) new JSONParser().parse(readFile(path));
            Iterator instancesItr = instancesArray.iterator();
            Integer maxId = -1;

            while (instancesItr.hasNext()) {
                JSONObject instance = (JSONObject) instancesItr.next();
                Integer id = Integer.parseInt((String) instance.get("id"));
                maxId = maxId > id ? maxId : id;
            }
            try {
                JSONObject newInstance = (JSONObject) new JSONParser().parse(data);
                newInstance.put("id", Integer.toString(maxId + 1));
                instancesArray.add(newInstance);
                writeFile(path, instancesArray.toJSONString());
            } catch (ParseException e) {
                throw new Error("400: Invalid data");
            }
            return "200: OK";
        } catch (Exception e) {
            return "500: " + e.getMessage();
        }
    }

    public static String DELETE(String query) {
        try {
            String path = checkAccess(query);
            HashMap<String, String> queryParams = getQueryParams(query);
            JSONArray instancesArray = (JSONArray) new JSONParser().parse(readFile(path));

            for (Object o: instancesArray.toArray()) {
                JSONObject instance = (JSONObject) o;
                boolean toBeDeleted = true;

                for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                    if (instance.containsKey(entry.getKey()) && !instance.get(entry.getKey()).equals(entry.getValue())) {
                        toBeDeleted = false;
                    }
                }

                if (toBeDeleted) {
                    instancesArray.remove(instance);
                }
            }

            writeFile(path, instancesArray.toJSONString());
            return "200: OK";
        } catch (Exception e) {
            return "500: " + e.getMessage();
        }
    }

    private static String checkAccess(String query) throws ParseException, IOException {
        Matcher matcherURL = URL_PARSE_REGEX.matcher(query);

        if (matcherURL.find()) {
            String host = matcherURL.group(1);
            String path = matcherURL.group(2);
            HashMap<String, String> queryParams = getQueryParams(query);
            String login = queryParams.get("login");
            String password = queryParams.get("password");

            if (!host.equals(HOST)) {
                throw new Error("404: Not Found");
            }

            try {
                readFile(path);
            } catch (IOException e) {
                throw new Error("404: Not Found");
            }

            JSONArray adminsArray = (JSONArray) new JSONParser().parse(readFile("/admins"));
            Iterator adminsItr = adminsArray.iterator();

            while (adminsItr.hasNext()) {
                JSONObject admin = (JSONObject) adminsItr.next();
                if (admin.get("login").equals(login) && admin.get("password").equals(password)) {
                    return path;
                }
            }
            throw new Error("400: Access Forbidden");
        } else {
            throw new Error("404: Not Found");
        }
    }

    private static HashMap<String, String> getQueryParams(String query)  {
        HashMap<String, String> queryParams = new HashMap<>();
        Matcher matcherParams = QUERY_PARAMS_REGEX.matcher(query);

        while (matcherParams.find()) {
            queryParams.put(matcherParams.group(1), matcherParams.group(2));
        }
        return queryParams;
    }

    private static String readFile(String instance) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(MessageFormat.format("data{0}.json", instance)));
        return new String(encoded, Charset.defaultCharset());
    }

    private static void writeFile(String instance, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(MessageFormat.format("data{0}.json", instance)));
        writer.write(content);
        writer.close();
    }
}
