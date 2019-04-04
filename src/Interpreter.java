import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

public class Interpreter extends ASTNodeVisitor {
    private ConnectInfo connectInfo = null;

    public Interpreter(ASTNode root) {
        super(root);
    }

    public void execute() {
        this.visit(this.root);
    }

    @Override
    protected BlockValue visitBlock(ASTNode node) {
        BlockValue block = super.visitBlock(node);

        if (block.name.equals("!connect")) {
            String host = null;
            String login = null;
            String password = null;

            for (FunctionValue function : block.statements.get(0)) {
                switch (function.name) {
                    case "host": host = function.valueParam; break;
                    case "login": login = function.valueParam; break;
                    case "password": password = function.valueParam; break;
                }
            }
            this.connectInfo = new ConnectInfo(host, login, password);
        } else {
            String instance = block.name.substring(1);
            for (ArrayList<FunctionValue> statement: block.statements) {
                JSONArray objects = new JSONArray();
                boolean created = false;

                for (int i = 0; i < statement.size(); i++) {
                    FunctionValue function = statement.get(i);

                    try {
                        if (i == 0) {
                            if (function.name.equals("get")) {
                                objects = (JSONArray) new JSONParser().parse(request("GET", instance, function.namedParams, null));
                            } else if (function.name.equals("add")) {
                                objects = (JSONArray) new JSONParser().parse("[]");
                                JSONObject newObject = (JSONObject) new JSONParser().parse("{}");

                                for (Pair<String, String> param: function.namedParams) {
                                    newObject.put(param.getKey(), param.getValue());
                                }
                                objects.add(newObject);
                                created = true;
                            }
                        } else if (i == statement.size() - 1) {
                            if (function.name.equals("print")) {
                                if (function.valueParam != null) {
                                    writeFile(function.valueParam, objects.toJSONString());
                                } else {
                                    System.out.println(objects);
                                }
                            } else if (function.name.equals("count")) {
                                System.out.println(objects.size());
                            } else if (function.name.equals("save")) {
                                if (created) {
                                    for (Object o: objects) {
                                        JSONObject object = (JSONObject) o;
                                        request("PUT", instance, new ArrayList<>(), object.toJSONString());
                                    }
                                } else {
                                    for (Object o: objects) {
                                        JSONObject object = (JSONObject) o;
                                        ArrayList<Pair<String, String>> params = new ArrayList<>();
                                        params.add(new Pair<>("id", (String) object.get("id")));
                                        request("POST", instance, params, object.toJSONString());
                                    }
                                }
                            } else if (function.name.equals("delete")) {
                                if (!created) {
                                    for (Object o: objects) {
                                        JSONObject object = (JSONObject) o;
                                        ArrayList<Pair<String, String>> params = new ArrayList<>();
                                        params.add(new Pair<>("id", (String) object.get("id")));
                                        request("DELETE", instance, params, null);
                                    }
                                }
                            }
                        } else {
                            if (function.name.equals("min")) {
                                for (Object o: objects.toArray()) {
                                    JSONObject object = (JSONObject) o;
                                    for (Pair<String, String> param: function.namedParams) {
                                        if (object.containsKey(param.getKey()) && ((String) object.get(param.getKey())).compareTo(param.getValue()) < 0) {
                                            objects.remove(object);
                                        }
                                    }
                                }
                            } else if (function.name.equals("max")) {
                                for (Object o: objects.toArray()) {
                                    JSONObject object = (JSONObject) o;
                                    for (Pair<String, String> param: function.namedParams) {
                                        if (object.containsKey(param.getKey()) && ((String) object.get(param.getKey())).compareTo(param.getValue()) > 0) {
                                            objects.remove(object);
                                        }
                                    }
                                }
                            } else if (function.name.equals("filter")) {
                                for (Object o: objects.toArray()) {
                                    JSONObject object = (JSONObject) o;
                                    for (Pair<String, String> param: function.namedParams) {
                                        if (object.containsKey(param.getKey()) && !object.get(param.getKey()).equals(param.getValue())) {
                                            objects.remove(object);
                                        }
                                    }
                                }
                            } else if (function.name.equals("set")) {
                                for (Object o: objects.toArray()) {
                                    JSONObject object = (JSONObject) o;
                                    for (Pair<String, String> param: function.namedParams) {
                                        object.put(param.getKey(), param.getValue());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return block;
    }

    private String request(String method, String instance, ArrayList<Pair<String, String>> params, String data) {
        String query = MessageFormat.format("{0}/{1}?login={2}&password={3}", this.connectInfo.host, instance, this.connectInfo.login, this.connectInfo.password);

        for (Pair<String, String> param: params) {
            query += MessageFormat.format("&{0}={1}", param.getKey(), param.getValue());
        }
        System.out.println(method + ": " + query);
        switch (method) {
            case "GET": return API.GET(query);
            case "POST": return API.POST(query, data);
            case "PUT": return API.PUT(query, data);
            case "DELETE": return API.DELETE(query);
            default: throw new Error("Unsupported method: " + method);
        }
    }

    private static void writeFile(String name, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(name));
        writer.write(content);
        writer.close();
    }
}
