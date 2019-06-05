package com.sd.coursework.API;

import android.os.AsyncTask;

import com.sd.coursework.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SuggestionFetcher extends AsyncTask<String,Void,Boolean> {
    private final String API_KEY = BuildConfig.APIKEY1;
    private final String URL_PREFIX = "https://dictionaryapi.com/api/v3/references/collegiate/json/";
    private final String URL_SUFFIX = "?key=" + API_KEY;
    private final String DICT_NAME = "Merriam Webster";
    private Suggestion.OnTaskCompleted listener;

    public SuggestionFetcher(Suggestion.OnTaskCompleted listener) {
        this.listener=listener;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        String urlPath = URL_PREFIX + strings[0] + URL_SUFFIX;

        try {
            URL url = new URL(urlPath);

            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line = "";
            StringBuilder jsonSB = new StringBuilder();
            while (line != null) {
                line = bufferedReader.readLine();
                jsonSB.append(line);
            }

            ResultQuery.clear();
            ResultQuery.setQuery(strings[0]);

            try {
                JSONArray arr = new JSONArray(jsonSB.toString());
                mainLoop: for (int i = 0; i <arr.length(); i++) {
                    Object json = new JSONTokener(arr.get(i).toString()).nextValue();
                    if (!(json instanceof JSONObject)) {
                        break;
                    }

                    JSONObject obj = arr.optJSONObject(i);

                    if (obj!=null && obj.has("meta")) {
                        JSONObject objMeta = obj.getJSONObject("meta");

                        Suggestion word = new Suggestion(DICT_NAME);
                        Suggestion.Meta meta = word.new Meta();
                        String[] id = objMeta.opt("id").toString().split(":");

                        switch (id.length) {
                            case 2:
                                meta.wordNumber = id[1];
                                //fall through

                            case 1:
                                meta.wordName = id[0];
                                meta.functionalLabel = obj.optString("fl");
                                break;

                            default:
                                continue; //if response doesn't have id, then something is wrong, therefore skip
                        }

                        if (obj.has("def")) {
                            JSONArray defArr = obj.getJSONArray("def");

                            for (int j = 0; j <defArr.length(); j++) {
                                JSONObject defObj = defArr.getJSONObject(j);
                                String sharedVD = (String)defObj.opt("vd");

                                if (defObj.has("sseq")) {
                                    JSONArray seqArr = defObj.getJSONArray("sseq");

                                    for (int k = 0; k <seqArr.length(); k++) {
                                        JSONArray senseArr = seqArr.getJSONArray(k);

                                        parseSenses(word, meta, sharedVD,senseArr);
                                        if (isCancelled()) break mainLoop;
                                    }
                                }
                            }

                            word.addDefinition(meta);
                            ResultQuery.addWord(word);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void parseSenses(Suggestion word, Suggestion.Meta meta,
                             String sharedVD, JSONArray senseArr) throws JSONException {

        String bsPrefix = "";
        for (int l = 0; l <senseArr.length(); l++) {
            Suggestion.Sense sense = word.new Sense();
            sense.verbDivider = sharedVD;

            boolean bs = false;
            JSONObject dtObj;
            if (senseArr.get(l).toString().contains("\"pseq\",")) {
                JSONArray pseqArr = senseArr.getJSONArray(l);
                parseSenses(word, meta, sharedVD, pseqArr.getJSONArray(1));
                continue;

            } else if (senseArr.get(l).toString().contains("\"bs\",")) {
                dtObj = senseArr.getJSONArray(l).getJSONObject(1).getJSONObject("sense");
                bs =true;

            } else {
                dtObj = senseArr.getJSONArray(l).getJSONObject(1);
            }

            if (dtObj.has("dt")) {
                JSONArray dtArr = dtObj.getJSONArray("dt");

                Suggestion.Def def = word.new Def();

                if (dtArr.length() > 1) { // 2 to 6: Might have example sentence(s)
                    for (int m = 0; m <dtArr.length(); m++) {
                        JSONArray dtArr1 = dtArr.getJSONArray(m);

                        if (dtArr1.get(0).toString().equals("vis")) {
                            JSONArray dtArr2 = dtArr1.getJSONArray(1);
                            for (int n = 0; n <dtArr2.length(); n++) {
                                JSONObject visObj = dtArr2.getJSONObject(n);
                                def.vis.add(visObj.opt("t").toString());
                            }
                        }
                    }
                }

                if (dtArr.length() > 0) { // 1: Has Definition
                    JSONArray dtArr2 = dtArr.getJSONArray(0);

                    if (!dtArr2.optString(0).equals("uns")) {
                        String formattedDef = new TextFormatter().parse( dtArr2.getString(dtArr2.length()-1) );
                        if (!formattedDef.isEmpty()) {
                            if (bs) {
                                bsPrefix = formattedDef;
                                continue;

                            } else {
                                String defS = "";
                                if (!bsPrefix.equals("")) {
                                    defS = bsPrefix + " ";
                                }

                                def.dt = defS + formattedDef;
                                sense.def = def;
                            }
                        }
                    }
                }
            }
            if (sense.def==null) return; //if no dt, then something is wrong, therefore skip

            sdsense: if (dtObj.has("sdsense")) {
                JSONArray dtArr = dtObj.getJSONObject("sdsense").getJSONArray("dt");

                Suggestion.Def def = word.new Def();

                if (dtArr.length() > 1) { // 2 to 6: Might have example sentence(s)
                    for (int m = 0; m <dtArr.length(); m++) {
                        JSONArray dtArr1 = dtArr.getJSONArray(m);

                        if (dtArr1.get(0).toString().equals("vis")) {
                            JSONObject visObj = dtArr1.getJSONArray(1).getJSONObject(0);
                            def.vis.add(visObj.opt("t").toString());
                        }
                    }
                }

                if (dtArr.length() > 0) { // 1: Has Definition
                    JSONArray dtArr2 = dtArr.getJSONArray(0);
                    def.dt = dtArr2.getString(dtArr2.length()-1);

                } else {
                    break sdsense; //if no definition, then something is wrong, therefore skip
                }
                sense.sdSenses.add(def);
            }

            meta.addSense(sense);

            if (isCancelled()) break;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        listener.onTaskCompleted(result);
    }
}