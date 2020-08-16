package googledrive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DriveFolderCreator {

	private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";

	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	// Directory to store user credentials for this application.
	public static final java.io.File CREDENTIALS_FOLDER //
			= new java.io.File(System.getProperty("user.home"), "credentials");

	public static final String CLIENT_SECRET_FILE_NAME = "client_secret.json";

	//
	// Global instance of the scopes required by this quickstart. If modifying these
	// scopes, delete your previously saved credentials/ folder.
	//
	private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

		java.io.File clientSecretFilePath = new java.io.File(CREDENTIALS_FOLDER, CLIENT_SECRET_FILE_NAME);

		if (!clientSecretFilePath.exists()) {
			throw new FileNotFoundException("Please copy " + CLIENT_SECRET_FILE_NAME //
					+ " to folder: " + CREDENTIALS_FOLDER.getAbsolutePath());
		}

		// Load client secrets.
		InputStream in = new FileInputStream(clientSecretFilePath);

		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(CREDENTIALS_FOLDER))
						.setAccessType("offline").build();

		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
	}

	public static boolean checkCredentialFolder() {

		if (!CREDENTIALS_FOLDER.exists()) {
			CREDENTIALS_FOLDER.mkdirs();
			return false;
		}
		if (CREDENTIALS_FOLDER.listFiles().length == 0) {
			return false;
		}
		java.io.File clientSecretFilePath = new java.io.File(CREDENTIALS_FOLDER, CLIENT_SECRET_FILE_NAME);

		if (!clientSecretFilePath.exists()) {
			return false;
		}
		return true;
	}

	public static void createDriveFolder(String filePath, String rootDir) {
		// System.out.println("CREDENTIALS_FOLDER: " +
		// CREDENTIALS_FOLDER.getAbsolutePath());
		// 1: Create CREDENTIALS_FOLDER
		try {
			checkCredentialFolder();
			// 2: Build a new authorized API client service.
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

			// 3: Read client_secret.json file & create Credential object.
			Credential credential = getCredentials(HTTP_TRANSPORT);

			// 5: Create Google Drive Service.
			Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential) //
					.setApplicationName(APPLICATION_NAME).build();

			// Print the names and IDs for up to 10 files.
			FileList result = service.files().list().setQ("name = '" + rootDir + "'").execute();// service.files().list().setPageSize(10).setFields("nextPageToken,
			String folderId = result.getFiles().get(0).getId();

			Map<String, String> nameList = getNameList(filePath);
			for (Entry<String, String> record : nameList.entrySet()) {
				File fileMetadata = new File();
				fileMetadata.setName(record.getValue());
				fileMetadata.setParents(Collections.singletonList(folderId));
				fileMetadata.setMimeType("application/vnd.google-apps.folder");
				File file = service.files().create(fileMetadata).setFields("id, parents").execute();
				insertPermission(service, file.getId(), record.getKey(), "user", "writer");
			}
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

	private static Permission insertPermission(Drive service, String fileId, String value, String type, String role) {
		Permission newPermission = new Permission();
		newPermission.setEmailAddress(value);
		newPermission.setType(type);
		newPermission.setRole(role);
		try {
			return service.permissions().create(fileId, newPermission).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Map<String, String> getNameList(String filePath) {
		java.io.File file = new java.io.File(filePath);
		Map<String, String> nameList = new HashMap<String, String>();
		try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {

			String line;
			while ((line = br.readLine()) != null) {
				String[] record = line.split(",");
				nameList.put(record[1], record[0]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // reads the file
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return nameList;
	}

}
