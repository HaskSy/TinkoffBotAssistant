package com.tinkoffbot.services;

import com.google.api.client.http.FileContent;
import com.tinkoffbot.ErrorEnum;
import com.tinkoffbot.model.ReportData;
import com.tinkoffbot.model.UserData;
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
import java.text.SimpleDateFormat;

import java.util.*;

public class GoogleServices {

    private static final String APPLICATION_GOOGLE_DRIVE_FOLDER = "tba_spreadsheets"; // App's folder name on user's Google Drive
    private static final String SPREADSHEET_PREFIX = "tba_stats_"; // Prefix of created by app spreadsheets
    private static final String REPORT_SPREADSHEET_PREFIX = "tba_TSV_"; // Prefix of reported questions spreadsheets
    private static final String ID_NAME_FILE = "id-name-file"; // Name of file with id->name dictionary

    private static final String APPLICATION_NAME_SHEETS = "sheets_test_name";
    private static final String APPLICATION_NAME_DRIVE = "drive_test_name";

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json"; // App's credentials file
    private static final String TOKENS_DIRECTORY_PATH = "tokens"; // Login token directory

    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS, DriveScopes.DRIVE_FILE); // App's working scopes

    /** After run necessary ID's will be stored in vars (To reduce requests count) **/

    private static String folderId = null;
    private static String idNameFileId = null;
    private static String currentSheetId = null;
    private static String currentReportSheetId = null;

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleServices.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: ".concat(CREDENTIALS_FILE_PATH));
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


    /** Finding app's folder on Google Drive or Initializing new one **/
    public static void setFolderId() throws IOException, GeneralSecurityException {

        Drive driveService = getDriveService();

        if (folderId != null) {
            return;
        }

        FileList result = driveService.files().list()
                .setQ("trashed = false and 'root' in parents and mimeType = 'application/vnd.google-apps.folder' ")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        for (File file : result.getFiles()) {
            if (file.getName().equals(APPLICATION_GOOGLE_DRIVE_FOLDER)) {
                folderId = file.getId();
                break;
            }
        }

        if (folderId == null) {
            File fileMetadata = new File();
            fileMetadata.setName(APPLICATION_GOOGLE_DRIVE_FOLDER);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            folderId = driveService.files().create(fileMetadata)
                    .setFields("id")
                    .execute().getId();

        }
    }

    /** Finding app's id-name dict on Google Drive or Initializing new one **/
    public static void setIdNameFile(String programFolderId) throws GeneralSecurityException, IOException {

        if (idNameFileId != null) {
            return;
        }

        Drive driveService = getDriveService();

        FileList result = driveService.files().list()
                .setQ("trashed = false and '".concat(folderId).concat("' in parents and mimeType = 'text/plain' "))
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name)")
                .execute();


        for (File file : result.getFiles()) {
            if (file.getName().equals(ID_NAME_FILE)) {
                idNameFileId = file.getId();
                break;
            }
        }

        if (idNameFileId == null) {
            File fileMetadata = new File();
            fileMetadata.setName(ID_NAME_FILE);
            fileMetadata.setParents(Collections.singletonList(programFolderId));
            fileMetadata.setMimeType("text/plain");

            java.io.File tmp = new java.io.File("tmp");
            new FileWriter(tmp).close();
            FileContent mediaContent = new FileContent("text/plain", tmp);

            File file = driveService.files().create(fileMetadata, mediaContent).setFields("id").execute();

            idNameFileId = file.getId();
        }
    }

//    private static String readIdFromFile() throws FileNotFoundException {
//        Scanner sc = new Scanner(new java.io.File(GoogleServices.FOLDER_ID_PATH));
//        if (sc.hasNextLine()) {
//            String line = sc.nextLine();
//            sc.close();
//            return line;
//        }
//        return null;
//    }
//
//    private static void writeIdFromFile(String id) throws IOException {
//        FileWriter writer = new FileWriter(GoogleServices.FOLDER_ID_PATH);
//        writer.append(id);
//        writer.close();
//    }

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
                Arrays.asList("ФИО", "КК", "ДК", "ТИ", "СИМ", "МНП", "ВС", "Last Update")
        ));

        getSheetsService().spreadsheets().values()
                .append(file.getId(), "Лист1", appendBody)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true)
                .execute();

        return file.getId();
    }

    private static String createNewReportSheet(String title, String programFolderId) throws IOException, GeneralSecurityException {

        File fileMetadata = new File();
        fileMetadata.setName(title);
        fileMetadata.setParents(Collections.singletonList(programFolderId));
        fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");

        File file = getDriveService().files().create(fileMetadata)
                .setFields("id, name")
                .set("namedRanges", Collections.singletonList(new NamedRange().setName("report_stats")).toString())
                .execute();

        ValueRange appendBody = new ValueRange().setValues(Collections.singletonList(
                Arrays.asList("ФИО", "Id активности", "Вопрос")
        ));

        getSheetsService().spreadsheets().values()
                .append(file.getId(), "Лист1", appendBody)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true)
                .execute();

        return file.getId();
    }

    private static ValueRange uploadDataInSheet(UserData userData, String spreadsheetId, int timestamp) throws IOException, GeneralSecurityException {

        List<Object> userDataList = Arrays.asList(userData.getName(), userData.getKK(), userData.getDK(), userData.getTI(), userData.getSIM(), userData.getMNP(), userData.getVS());

        ValueRange appendBody = new ValueRange();


        ValueRange values = getSheetsService().spreadsheets().values().get(spreadsheetId, "Лист1").execute();

        List<List<Object>> listList = values.getValues();

        boolean updating = false;

        int i = 1;
        for (; i < listList.size(); i++) {
            List<Object> row = listList.get(i);
            if (String.valueOf(row.get(0)).equals(String.valueOf(userDataList.get(0)))) {
                appendBody.setValues(Collections.singletonList(
                        Arrays.asList(String.valueOf(userDataList.get(0)),
                                Integer.parseInt(String.valueOf(row.get(1))) + Integer.parseInt(String.valueOf(userDataList.get(1))),
                                Integer.parseInt(String.valueOf(row.get(2))) + Integer.parseInt(String.valueOf(userDataList.get(2))),
                                Integer.parseInt(String.valueOf(row.get(3))) + Integer.parseInt(String.valueOf(userDataList.get(3))),
                                Integer.parseInt(String.valueOf(row.get(4))) + Integer.parseInt(String.valueOf(userDataList.get(4))),
                                Integer.parseInt(String.valueOf(row.get(5))) + Integer.parseInt(String.valueOf(userDataList.get(5))),
                                Integer.parseInt(String.valueOf(row.get(6))) + Integer.parseInt(String.valueOf(userDataList.get(6))))
                        )
                );
                updating = true;
                break;
            }
        }

        if (!updating) {

            appendBody.setValues(Collections.singletonList(userDataList));

            getSheetsService().spreadsheets().values()
                    .append(spreadsheetId, "Лист1", appendBody)
                    .setValueInputOption("USER_ENTERED")
                    .setInsertDataOption("INSERT_ROWS")
                    .setIncludeValuesInResponse(true)
                    .execute();

        } else {

            getSheetsService().spreadsheets().values()
                    .update(spreadsheetId, "A".concat(Integer.toString(i + 1)).concat(":G").concat(Integer.toString(i + 1)), appendBody)
                    .setValueInputOption("RAW")
                    .execute();

        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long)timestamp*1000);
        SimpleDateFormat outputFmt = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        outputFmt.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));

        getSheetsService().spreadsheets().values()
                .update(spreadsheetId, "H2",
                        new ValueRange().setValues(
                                Collections.singletonList(
                                        Collections.singletonList(
                                                outputFmt.format(calendar.getTime())))))
                .setValueInputOption("RAW")
                .execute();

        return appendBody;

    }


    private static ErrorEnum uploadReportDataInSheet(ReportData reportData, String spreadsheetId) throws IOException, GeneralSecurityException {

        List<Object> userDataList = Arrays.asList(reportData.getFio(), reportData.getActivityId(), reportData.getQuestion());

        ValueRange appendBody = new ValueRange();

        ValueRange values = getSheetsService().spreadsheets().values().get(spreadsheetId, "Лист1").execute();

        List<List<Object>> listList = values.getValues();


        int i = 1;
        for (; i < listList.size(); i++) {
            List<Object> row = listList.get(i);
            if (String.valueOf(row.get(1)).equals(String.valueOf(userDataList.get(1)))) {
                return ErrorEnum.SAME_ACTIVITY_ID_ERROR;
            }
        }

        appendBody.setValues(Collections.singletonList(userDataList));

        getSheetsService().spreadsheets().values()
                .append(spreadsheetId, "Лист1", appendBody)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true)
                .execute();

        return null;

    }



    private static void updatePlainText(int userID, String name, String fileId, int timestamp) throws GeneralSecurityException, IOException {

        Drive drive = getDriveService();

        InputStream in = drive.files().get(fileId).executeMedia().getContent();

        Scanner sc = new Scanner(in);
        StringBuilder buffer = new StringBuilder();

        boolean switched = false;

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (!switched && line.startsWith(String.valueOf(userID))) {
                buffer.append(userID).append("->").append(name);
                GoogleServices.updateName(line.split("->")[1], name, GoogleServices.getCurrentSpreadsheet(timestamp));
                switched = true;
            }
            else {
                buffer.append(line);
            }
            buffer.append(System.lineSeparator());
        }

        if (!switched) {
            buffer.append(userID).append("->").append(name);
        }

        sc.close();

        java.io.File updatedFile = new java.io.File("tmp");
        FileWriter writer = new FileWriter(updatedFile);
        writer.append(buffer.toString());
        writer.close();

        File fileObjectWithUpdates = new File();
        fileObjectWithUpdates.setMimeType("text/plain");
        fileObjectWithUpdates.setName(ID_NAME_FILE);

        drive.files().update(fileId, fileObjectWithUpdates, new FileContent("text/plain", updatedFile)).execute();
    }

    public static void processRegMessage(int userID, String name, int timestamp) throws IOException, GeneralSecurityException {

        setIdNameFile(folderId);

        updatePlainText(userID, name, idNameFileId, timestamp);

    }

    public static List<Object> processSentMessage(UserData userData, Integer timestamp) throws GeneralSecurityException, IOException {

        List<Object> ans;

        String currentSheetId = getCurrentSpreadsheet(timestamp);

        ans = uploadDataInSheet(userData, currentSheetId, timestamp).getValues().get(0);

        return ans;

    }

    public static ErrorEnum processReportMessage(ReportData reportData, Integer timestamp) throws IOException, GeneralSecurityException {
        String currentReportSheetId = getCurrentReportSpreadsheet(timestamp);

        return uploadReportDataInSheet(reportData, currentReportSheetId);
    }

    public static void updateName(String prevName, String newName, String spreadsheetId) throws IOException, GeneralSecurityException {
        List<List<Object>> sheetInfo = getSheetsService().spreadsheets().values().get(spreadsheetId, "Лист1").execute().getValues();

        boolean updating = false;

        int i = 1;
        for (; i < sheetInfo.size(); i++) {
            List<Object> row = sheetInfo.get(i);
            if (String.valueOf(row.get(0)).equals(prevName)) {
                updating = true;
                break;
            }
        }

        if (updating) {
            getSheetsService().spreadsheets().values()
                    .update(spreadsheetId, "A".concat(Integer.toString(i + 1)), new ValueRange().setValues(Collections.singletonList(Collections.singletonList(newName))))
                    .setValueInputOption("RAW")
                    .execute();
        }
    }

    public static String getCurrentSpreadsheet(int timestamp) throws IOException, GeneralSecurityException {

        if (currentSheetId != null) {
            return currentSheetId;
        }

        FileList sheetsList = getDriveService().files().list()
                .setQ("trashed = false and '".concat(folderId).concat("' in parents and mimeType = 'application/vnd.google-apps.spreadsheet' "))
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name)")
                .execute();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long)timestamp*1000);
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));


        String currentSheetName;

        if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER && calendar.get(Calendar.DATE) > 19) {
            SimpleDateFormat outputFmt = new SimpleDateFormat("yy");
            outputFmt.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
            currentSheetName = SPREADSHEET_PREFIX.concat("01.").concat(Integer.toString(Integer.parseInt(outputFmt.format(calendar.getTime())) + 1));
        }
        else {
            SimpleDateFormat outputFmt = new SimpleDateFormat("MM.yy");
            outputFmt.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
            currentSheetName = SPREADSHEET_PREFIX.concat(outputFmt.format(calendar.getTime()));

        }

        for (File file : sheetsList.getFiles()) {
            if (file.getName().equals(currentSheetName)) {
                currentSheetId = file.getId();
                break;
            }
        }

        if (currentSheetId == null) {
            currentSheetId = createNewSheet(currentSheetName, folderId);
        }

        return currentSheetId;
    }

    public static String getCurrentReportSpreadsheet(int timestamp) throws IOException, GeneralSecurityException {

        if (currentReportSheetId != null) {
            return currentReportSheetId;
        }

        FileList sheetsList = getDriveService().files().list()
                .setQ("trashed = false and '".concat(folderId).concat("' in parents and mimeType = 'application/vnd.google-apps.spreadsheet' "))
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name)")
                .execute();


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long)timestamp*1000);
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));


        String currentSheetName;

        if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER && calendar.get(Calendar.DATE) >= 19) {
            SimpleDateFormat outputFmt = new SimpleDateFormat("yy");
            outputFmt.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
            currentSheetName = REPORT_SPREADSHEET_PREFIX.concat("01.").concat(Integer.toString(Integer.parseInt(outputFmt.format(calendar.getTime())) + 1));
        }
        else {
            SimpleDateFormat outputFmt = new SimpleDateFormat("MM.yy");
            outputFmt.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
            currentSheetName = REPORT_SPREADSHEET_PREFIX.concat(outputFmt.format(calendar.getTime()));

        }

        for (File file : sheetsList.getFiles()) {
            if (file.getName().equals(currentSheetName)) {
                currentReportSheetId = file.getId();
                break;
            }
        }

        if (currentReportSheetId == null) {
            currentReportSheetId = createNewReportSheet(currentSheetName, folderId);
        }

        return currentReportSheetId;
    }


    public static String getNameById(int userId) throws IOException, GeneralSecurityException {

        setIdNameFile(folderId);

        InputStream in = getDriveService().files().get(idNameFileId).executeMedia().getContent();

        Scanner sc = new Scanner(in);

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.startsWith(String.valueOf(userId))) {
                sc.close();
                return line.split("->")[1];
            }
        }

        sc.close();
        return null;
    }

}