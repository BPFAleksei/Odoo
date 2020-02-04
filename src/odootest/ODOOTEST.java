/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package odootest;

import java.net.URL;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

/**
 *
 * @author aleksei.escobar
 */
public class ODOOTEST {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            final String url, db, username, password;
            final XmlRpcClient client = new XmlRpcClient();
            //ENDPOINT TO START
            final XmlRpcClientConfigImpl start_config = new XmlRpcClientConfigImpl();
            start_config.setServerURL(new URL("https://demo.odoo.com/start"));
            final Map<String, String> info = (Map<String, String>) client.execute(
                    start_config, "start", emptyList());
            url = info.get("host");
            db = info.get("database");
            username = info.get("user");
            password = info.get("password");
            System.out.println(info.get("host"));
            System.out.println(info.get("database"));
            System.out.println(info.get("user"));
            System.out.println(info.get("password"));

            //------------------ENDPOINT TO LOGGING IN
            final XmlRpcClientConfigImpl common_config = new XmlRpcClientConfigImpl();
            common_config.setServerURL(
                    new URL(String.format("%s/xmlrpc/2/common", url)));
            final Map<String, String> version = (Map<String, String>) client.execute(common_config, "version", emptyList());
            System.out.println(version);

            //----Authentication
            int uid = (int) client.execute(
                    common_config, "authenticate", asList(
                            db, username, password, emptyMap()));

            System.out.println("Login" + uid);
            //----ENDPOINT TO CALL METHODS
            final XmlRpcClient models = new XmlRpcClient() {
                {
                    setConfig(new XmlRpcClientConfigImpl() {
                        {
                            setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
                        }
                    });
                }
            };

            //Verificar si tengo acceso al modelo res.partner
            models.execute("execute_kw", asList(
                    db, uid, password,
                    "res.partner", "check_access_rights",
                    asList("read"),
                    new HashMap() {
                {
                    put("raise_exception", false);
                }
            }));
            // LIST RECORDS
            java.util.List userData = asList((Object[]) models.execute("execute_kw", asList(
                    db, uid, password,
                    "res.partner", "search",
                    asList(asList(
                            asList("is_company", "=", true))) //FILTROS
            )));

            System.out.println(userData);

            Integer numRecords = (Integer) models.execute("execute_kw", asList(
                    db, uid, password,
                    "res.partner", "search_count",
                    asList(asList(
                            asList("is_company", "=", true)))
            ));
            System.out.println(numRecords);

            // READ RECORDS
            List ids = asList((Object[]) models.execute(
                    "execute_kw", asList(
                            db, uid, password,
                            "res.partner", "search",
                            asList(asList(
                                    asList("is_company", "=", true))),
                            new HashMap() {
                        {
                            put("limit", 1);
                        }
                    })));

            System.out.println("All IDs " + ids);

            Map record = (Map) ((Object[]) models.execute(
                    "execute_kw", asList(
                            db, uid, password,
                            "res.partner", "read",
                            asList(ids)
                    )
            ))[0];

            System.out.println("Records " + record.size());

            //elegir solo tres campos considerados interesantes
            List interesting = asList((Object[]) models.execute("execute_kw", asList(
                    db, uid, password,
                    "res.partner", "read",
                    asList(ids),
                    new HashMap() {
                {
                    put("fields", asList("name", "country_id", "comment"));
                }
            }
            )));

            System.out.println(interesting);
            //------------------Listing record fields-----------------
            Map<String, Map<String, Object>> listingFields = (Map<String, Map<String, Object>>) models.execute("execute_kw", asList(
                    db, uid, password,
                    "res.partner", "fields_get",
                    emptyList(),
                    new HashMap() {
                {
                    put("attributes", asList("string", "help", "type"));
                }
            }
            ));

            System.out.println("\n\nListed fields: " + listingFields);

            List<Object> searchResults = asList((Object[]) models.execute("execute_kw", asList(
                    db, uid, password,
                    "res.partner", "search_read",
                    asList(asList(
                            asList("is_company", "=", true))),
                    new HashMap() {
                {
                    put("fields", asList("name", "country_id", "comment"));
                    put("limit", 5);
                }
            }
            )));
            System.out.println("\n\nSearch Results: " + searchResults);

            final Integer id = (Integer) models.execute("execute_kw", asList(
                    db, uid, password,
                    "res.partner", "create",
                    asList(new HashMap() {
                        {
                            put("name", "New Partner Test");
                        }
                    })
            ));
            
            System.out.println("New Record id " + id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
