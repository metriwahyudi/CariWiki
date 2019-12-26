package find;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Aplikasi Pencarian Wikipedia
 *
 */
public class App {
    // Library untuk http request
    OkHttpClient client = new OkHttpClient();

    // API untuk melakukan pencarina
    public static String BASE_SEARCH = "https://en.wikipedia.org/w/api.php?action=query&list=search&utf8=&format=json&srsearch=";
    
    // API untuk mengambil detail singkat halaman
    public static String BASE_DETAIL = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro&explaintext&redirects=1&pageids=";
    
    // Library untuk mengambil inputan dari pengguna
    public Scanner cli = new Scanner(System.in);

    // Object/struct data per-row/list hasil pencarian
    class Result{
        public String title;
        public Integer pageid;
        public String summary;
    }
    
    // Array dimana variable Result listing
    public ArrayList<Result> list = new ArrayList<Result>(); 
    
    // Keyword pencarian
    public String keyword;

    /**
     * Fungsi utama yang akan dipangil
     * @param args
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {
        System.out.println("====================================== Aplikasi Pencarian Wikipedia ======================================");
        final App app = new App();
        final String Keyword = app.getUserKeyword();
        app.keyword = Keyword;
        final String response = app.run(BASE_SEARCH + Keyword);
        if (response != null) {
            app.responseHandler(response);
        }
        
        app.mainMenu();
    }

    /**
     * Fungsi untuk melakuan HTTP Request ke API
     * @param url
     * @return String|null
     */
    String run(final String url) {
        final Request request = new Request.Builder().url(url).build();
        System.out.println("Menghubungkan ke Wikipedia.org ....");
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (final IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Fungsi untuk memproses hasil pencarian
     * @param response
     */
    void responseHandler(final String response) {
        Boolean searchIsFound = false;
        JSONObject json = new JSONObject(response);
        if (json.has("query")) {
            JSONObject query = json.getJSONObject("query");
            if (query.has("search")) {
                JSONArray search = query.getJSONArray("search");
                if(search != null){
                    this.list.clear();
                    searchIsFound = true;
                    for(int i=0;i<search.length();i++){
                        JSONObject row = search.getJSONObject(i);
                        Result res = new Result();
                        res.title = row.getString("title");
                        res.pageid = row.getInt("pageid");
                        res.summary = null;
                        this.list.add(res);

                    }
                }
            }
        }
        if(searchIsFound){
            displayLastSearch(false);
        }else{
            System.out.println("-< Hasil tidak ditemukan >-");
            
        }
        
    }

    /**
     * Fungsi untuk membaca inputan keyword dari pengguna
     * @return
     */
    String getUserKeyword() {
        System.out.println("Masukkan Keyword:");
        return this.cli.nextLine();
    }

    /**
     * Fungsi yang menampilkan Menu Utama dan membaca pilihan pengguna
     * @return
     */
    String getUserChoice(){
        System.out.println("[q] Quit [r] Retry [l] Hasil terakhir");
        return this.cli.next();
        
    }

    /**
     * Fungsi untuk menampilkan hasil pencarian yang telah disimpan pada @variable list
     * Dengan parameter untuk menampilkan keyword terakhir atau tidak.
     * @param showKeyword
     */
    void displayLastSearch(Boolean showKeyword){
        
        System.out.println("----------------------------------------------------------------------------------------------------------");
        System.out.println("====================================== Hasil dari Wikipedia.org ==========================================");
        if(showKeyword){
            System.out.println("Hasil pencarian '"+this.keyword+"' ");
        }
        Integer iteration = 0;
        while(iteration < this.list.size()){
            Result result = this.list.get(iteration);
            System.out.println("\t"+iteration+" "+result.title);
            iteration++;
        }
        detailMenu();
        System.out.println("----------------------------------------------------------------------------------------------------------");
        
    }

    /**
     * Fungsi untuk menampilkan detail singkat hasil pencarian
     * Fungsi akan mengambil detail dengan mengirim permintaan ke API kemudian ditampilkan
     */
    void detailMenu(){
        System.out.println("Silakan pilih salah satu: ");
        Integer answer = cli.nextInt();
        Result selected = this.list.get(answer);
        if(selected == null){
            System.out.println("Yang anda pilih tidak ada didaftar.");
            
            return;
        }
        System.out.println(selected.title);
        Boolean getDetailIsOk = false;
        if(selected.summary == null){
            String response = run(BASE_DETAIL+selected.pageid);
            
            if(response != null){
                JSONObject json = new JSONObject(response);
                if(json.has("query")){
                    JSONObject query = json.getJSONObject("query");
                    if(query.has("pages")){
                        JSONObject pages = query.getJSONObject("pages");
                        if(pages.has(selected.pageid+"")){
                            JSONObject page = pages.getJSONObject(selected.pageid+"");
                            selected.summary = page.getString("extract");
                            this.list.set(answer, selected);
                            getDetailIsOk = true;
                        }
                    }
                }
                //System.out.println(response);
            }
            
        }else{
            getDetailIsOk = true;
        }
        if(getDetailIsOk){
            System.out.println("==========================================================================================================");
            System.out.println(this.list.get(answer).summary);
            System.out.println("----------------------------------------------------------------------------------------------------------");
        }else{
            System.out.println("Tidak dapat menemukan detailnya. Silakan coba lagi.");
        }
    }

    /**
     * Fungsi untuk memproses menu utama
     */
    void mainMenu() {
        System.out.println("=========================================== MAIN MENU ====================================================");
        String choice = getUserChoice();
        
        switch(choice) {
            case "q":
                quit();
              break;
            case "l":
                detail();
              break;
            default:
                retry();
          }
          
    }

    /**
     * Fungsi untuk menghentikan seluruh prosess
     */
    void quit(){
        System.out.println("...GOOD BY...");
        System.exit(0);
    }

    /**
     * Fungsi untuk kembali melakukan pencarian kembali
     */
    void retry(){
        try {
            find.App.main(null);
        } catch (final Exception e) {
            System.out.println("...GOOD BY...");
            System.exit(0);
        }
    }

    // Funsi yang mengarahkan ke fungsi displayLastSearch()
    void detail(){
        displayLastSearch(true);
        mainMenu();
    }
   
}
