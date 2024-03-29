package helperClasses;


import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import javax.imageio.ImageIO;
import com.flickr4java.flickr.*;
import com.flickr4java.flickr.photos.*;
import javafx.concurrent.Task;


public class FlickrClass extends Task<Void> {
    private static int _num;
    private static String _searchTerm, _path;

    /**
     * Creates a flickr task with the given search term
     * @param searchTerm
     * @param path
     */
    public FlickrClass(String searchTerm, String path){
        _searchTerm = searchTerm;
        _num = 10;
        _path = path;
    }

    /**
     * Gets the flickr api key, should be located in the same level as the jar/project
     * @param key
     * @return
     * @throws Exception
     */
    public String getAPIKey(String key) throws Exception {
        File file = new File("./flickr-api-keys.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));

        String line;
        while ( (line = br.readLine()) != null ) {
            if (line.trim().startsWith(key)) {
                br.close();
                return line.substring(line.indexOf("=")+1).trim();
            }
        }
        br.close();
        throw new RuntimeException("Couldn't find " + key +" in config file "+file.getName());
    }

    /**
     * Runs the flickr search and downloads images relating to the search term. The number of images downloaded is fixed to 10
     * @return
     */
    public Void call() {
        try {
            //NASSIR WROTE THIS CODE HE CAN EXPLAIN THIS
            String apiKey = getAPIKey("apiKey");
            String sharedSecret = getAPIKey("sharedSecret");

            Flickr flickr = new Flickr(apiKey, sharedSecret, new REST());

            String query = _searchTerm;
            int page = 0;

            PhotosInterface photos = flickr.getPhotosInterface();
            SearchParameters params = new SearchParameters();
            params.setSort(SearchParameters.RELEVANCE);
            params.setMedia("photos");
            params.setText(query);

            PhotoList<Photo> results = photos.search(params, _num, page);
            System.out.println("Retrieving " + results.size()+ " results");

            for (Photo photo: results) {
                try {
                    String cmd = "mkdir " + _path + "/temp";
                    ProcessBuilder builder = new ProcessBuilder("bash", "-c", cmd);
                    Process process = builder.start();
                    process.waitFor();
                    process.destroy();

                    BufferedImage image = photos.getImage(photo,Size.LARGE);
                    String filename = query.trim().replace(' ', '-')+"-"+System.currentTimeMillis()+"-"+photo.getId()+".jpg";
                    File outputfile = new File(_path + "/temp", filename);
                    ImageIO.write(image, "jpg", outputfile);
                    System.out.println("Downloaded "+filename);
                } catch (FlickrException fe) {
                    System.err.println("Ignoring image " +photo.getId() +": "+ fe.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\nDone");
        return null;
    }

}