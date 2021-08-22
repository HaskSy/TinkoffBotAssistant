package com.example.tinkoffbot.services;

import com.example.tinkoffbot.model.UserData;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import java.io.*;

import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GoogleSheetsService {

    //TODO: minimize amount of requests

    private static final String APPLICATION_GOOGLE_DRIVE_FOLDER = "tba_spreadsheets";
    private static final String SPREADSHEET_PREFIX = "tba_stats_";
    private static final String APPLICATION_NAME_SHEETS = "sheets_test_name";
    private static final String APPLICATION_NAME_DRIVE = "drive_test_name";

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS, DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleSheetsService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                clientSecrets,
                SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();


        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver.Builder().setPort(9090).build()).authorize("user");
    }

    private static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME_SHEETS)
                .build();
    }

    private static Drive getDriveService() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME_DRIVE)
                .build();
    }

    public static void processSentMessage(UserData userData) throws GeneralSecurityException, IOException {

        Drive driveService = getDriveService();

        String pageToken = null;

        do {
            FileList result = driveService.files().list()
                    .setQ("trashed = false and 'root' in parents and mimeType = 'application/vnd.google-apps.folder' ")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();

            String programFolderId = null;

            for (File file : result.getFiles()) {
                if (file.getName().equals(APPLICATION_GOOGLE_DRIVE_FOLDER)) {
                    programFolderId = file.getId();
                    break;
                }
            }

            if (programFolderId == null) {
                File fileMetadata = new File();
                fileMetadata.setName(APPLICATION_GOOGLE_DRIVE_FOLDER);
                fileMetadata.setMimeType("application/vnd.google-apps.folder");

                programFolderId = driveService.files().create(fileMetadata)
                        .setFields("id")
                        .execute().getId();
            }


            FileList sheetsList = driveService.files().list()
                    .setQ("trashed = false and '"+ programFolderId + "' in parents and mimeType = 'application/vnd.google-apps.spreadsheet' ")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();




            String currentSheetName = SPREADSHEET_PREFIX+LocalDateTime.now(ZoneId.of("Europe/Moscow"))
                    .format(DateTimeFormatter.ofPattern("MM.yyyy"));

            String currentSheetId = null;

            for (File file : sheetsList.getFiles()) {
                if (file.getName().equals(currentSheetName)) {
                    currentSheetId = file.getId();
                    break;
                }
            }

            if (currentSheetId == null) {
                currentSheetId = createNewSheet(currentSheetName, programFolderId);
            }

            uploadDataInSheet(userData, currentSheetId);

            pageToken = result.getNextPageToken();

        } while (pageToken != null);
    }

    private static String createNewSheet(String title, String programFolderId) throws IOException, GeneralSecurityException {

        File fileMetadata = new File();
        fileMetadata.setName(title);
        fileMetadata.setParents(Collections.singletonList(programFolderId));
        fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");

        File file = getDriveService().files().create(fileMetadata)
                .setFields("id, name")
                .set("namedRanges", Collections.singletonList(new NamedRange().setName("stats")).toString())
                .execute();

        ValueRange appendBody = new ValueRange().setValues(Collections.singletonList(
                Arrays.asList("ФИО", "КК", "ДК", "ТИ", "СИМ", "МНП")
        ));

        AppendValuesResponse valuesResponse = getSheetsService().spreadsheets().values()
                .append(file.getId(), "Лист1", appendBody)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true)
                .execute();


        return file.getId();
    }

    private static void uploadDataInSheet(UserData userData, String spreadsheetId) throws IOException, GeneralSecurityException {

        List<String> userDataList = Arrays.asList(userData.toString().split(" "));

        ValueRange appendBody = new ValueRange();


        ValueRange values = getSheetsService().spreadsheets().values().get(spreadsheetId, "Лист1").execute();

        List<List<Object>> listList = values.getValues();

        boolean updating = false;

        int i = 1;
        for (; i < listList.size(); i++) {
            List<Object> row = listList.get(i);
            if (row.get(0).toString().equals(userDataList.get(0))) {
                appendBody.setValues(Collections.singletonList(
                        Arrays.asList(userDataList.get(0),
                                Integer.parseInt(row.get(1).toString()) + Integer.parseInt(userDataList.get(1)),
                                Integer.parseInt(row.get(2).toString()) + Integer.parseInt(userDataList.get(2)),
                                Integer.parseInt(row.get(3).toString()) + Integer.parseInt(userDataList.get(3)),
                                Integer.parseInt(row.get(4).toString()) + Integer.parseInt(userDataList.get(4)),
                                Integer.parseInt(row.get(5).toString()) + Integer.parseInt(userDataList.get(5))
                        )
                ));
                updating = true;
                break;
            }
        }

        if (!updating) {

            appendBody.setValues(Collections.singletonList(Arrays.asList(userData.toString().split(" "))));

            AppendValuesResponse valuesResponse = getSheetsService().spreadsheets().values()
                    .append(spreadsheetId, "Лист1", appendBody)
                    .setValueInputOption("USER_ENTERED")
                    .setInsertDataOption("INSERT_ROWS")
                    .setIncludeValuesInResponse(true)
                    .execute();

        } else {

            UpdateValuesResponse result = getSheetsService().spreadsheets().values()
                    .update(spreadsheetId, "A"+ (i + 1) +":F"+ (i + 1), appendBody)
                    .setValueInputOption("RAW")
                    .execute();

        }

    }


}