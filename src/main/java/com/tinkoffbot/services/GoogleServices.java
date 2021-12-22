package com.tinkoffbot.services;

import com.google.api.client.http.FileContent;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;

import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;

import java.util.*;

public class GoogleServices {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(GoogleServices.class);

    private static final String APPLICATION_GOOGLE_DRIVE_FOLDER = "tba_spreadsheets"; // App's folder name on user's Google Drive

    private static final HashMap<SheetType, String> prefixes = new HashMap<>(Map.of(
            SheetType.STATS, "tba_stats_",
            SheetType.REPORT_STATS, "tba_TSV_")); // Created by app prefixes of stats spreadsheets & reported questions spreadsheets

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

    private static final HashMap<SheetType, String> currentSheetsId = new HashMap<>();

    private static @NotNull Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
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

    private static @NotNull Sheets getSheetsService() throws IOException, GeneralSecurityException {

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME_SHEETS)
                .build();
    }

    private static @NotNull Drive getDriveService() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME_DRIVE)
                .build();
    }

    /**
     * Finding app's folder on Google Drive or Initializing new one
     *
     */
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

    /**
     * Finding app's id-name dict on Google Drive or Initializing new one
     *
     */
    private static void setIdNameFile() throws GeneralSecurityException, IOException {

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
            fileMetadata.setParents(Collections.singletonList(folderId));
            fileMetadata.setMimeType("text/plain");

            java.io.File tmp = new java.io.File("tmp");
            new FileWriter(tmp).close();
            FileContent mediaContent = new FileContent("text/plain", tmp);

            File file = driveService.files().create(fileMetadata, mediaContent).setFields("id").execute();

            idNameFileId = file.getId();
        }
    }

    /**
     * Initializing new spreadsheet of sheetType type in app's folder
     *
     * @param title Name of spreadsheet in Google Drive
     * @param sheetType Report/Stats spreadsheet type
     * @param headers First row of spreadsheet (column titles)
     * @return returns newSheetId
     */
    private static String createNewSheet(String title, @NotNull SheetType sheetType, List<Object> headers) throws GeneralSecurityException, IOException {
        File fileMetadata = new File();
        fileMetadata.setName(title);
        fileMetadata.setParents(Collections.singletonList(folderId));
        fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");

        File file = getDriveService().files().create(fileMetadata)
                .setFields("id, name")
                .set("namedRanges", Collections.singletonList(new NamedRange().setName(sheetType.getType())).toString())
                .execute();

        ValueRange appendBody = new ValueRange().setValues(Collections.singletonList(headers));

        getSheetsService().spreadsheets().values()
                .append(file.getId(), "Лист1", appendBody)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true)
                .execute();

        return file.getId();
    }

    /**
     *  Overloaded createNewSheet method
     *
     * @param title Name of spreadsheet in Google Drive
     * @param sheetType Report/Stats spreadsheet type
     * @return returns newSheetId
     **/
    private static @Nullable String createNewSheet(String title, @NotNull SheetType sheetType) throws GeneralSecurityException, IOException {
        if (sheetType.equals(SheetType.STATS)) {
            return createNewSheet(title, sheetType,
                    Arrays.asList("ФИО", "КК", "ДК", "ТИ", "СИМ", "МНП", "ВС", "Last Update"));
        }
        else if (sheetType.equals(SheetType.REPORT_STATS)) {
            return createNewSheet(title, sheetType,
                    Arrays.asList("ФИО", "Id активности", "Вопрос"));
        }
        return null;
    }

    /**
     * Adding new row of data or updating existed
     * Updating data in current stats spreadsheet
     *
     * @param userData parsed data from group chat
     * @param spreadsheetId ID of current month stats spreadsheet
     * @param timestamp time when message was received
     * @return returns ValueRange of updated row
    */
    private static @NotNull ValueRange uploadDataInSheet(@NotNull UserData userData, String spreadsheetId, int timestamp) throws IOException, GeneralSecurityException {

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

    /**
     * Adding new report message & checking iff every single ID is unique
     *
     * @param reportData parsed data from group chat
     * @param spreadsheetId ID of current month report spreadsheet
     * @return returns SAME_ACTIVITY_ID_ERROR (ErrorEnum) if report with same ID already exists
     */
    private static @Nullable ErrorEnum uploadReportDataInSheet(@NotNull ReportData reportData, String spreadsheetId) throws IOException, GeneralSecurityException {

        List<Object> userDataList = Arrays.asList(reportData.getFsl(), reportData.getActivityId(), reportData.getQuestion());

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

    /**
     * Updating located on Google Drive file with pair telegramId-Name
     * or Adding new telegramId-Name pair
     *
     * @param userID telegram ID of user
     * @param name new/updated UserName
     * @param timestamp time, when message was received
     */
    private static void updateIdNameDict(int userID, String name, int timestamp) throws GeneralSecurityException, IOException {

        Drive drive = getDriveService();

        InputStream in = drive.files().get(idNameFileId).executeMedia().getContent();

        Scanner sc = new Scanner(in);
        StringBuilder buffer = new StringBuilder();

        boolean switched = false;

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (!switched && line.startsWith(String.valueOf(userID))) {
                buffer.append(userID).append("->").append(name);
                GoogleServices.updateName(line.split("->")[1], name, GoogleServices.getCurrentSpreadsheet(timestamp, SheetType.STATS));
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

        drive.files().update(idNameFileId, fileObjectWithUpdates, new FileContent("text/plain", updatedFile)).execute();
    }

    /**
     * Starts processing registration-type (/reg) message
     *
     * @param userID telegram ID of user
     * @param name new/updated UserName
     * @param timestamp time, when message was received
     * */
    public static void processRegMessage(int userID, String name, int timestamp) throws IOException, GeneralSecurityException {

        setIdNameFile(); //find current Id-Name dict on Google Drive If exists or creating new

        updateIdNameDict(userID, name, timestamp);

    }

    /**
     * Starts processing stats-type (/send) message
     *
     * @param userData parsed data from message
     * @param timestamp time, when message was received
     * @return returns List<Object> of updated or added row
     * */
    public static List<Object> processSentMessage(UserData userData, Integer timestamp) throws GeneralSecurityException, IOException {

        List<Object> ans;

        String currentSheetId = getCurrentSpreadsheet(timestamp, SheetType.STATS);

        ans = uploadDataInSheet(userData, currentSheetId, timestamp).getValues().get(0);

        return ans;

    }

    /**
     * Starts processing report-type message
     *
     * @param reportData parsed data from message
     * @param timestamp time, when message was received
     * @return returns Error of existing ID if present
     * */
    public static ErrorEnum processReportMessage(ReportData reportData, Integer timestamp) throws IOException, GeneralSecurityException {
        String currentReportSheetId = getCurrentSpreadsheet(timestamp, SheetType.REPORT_STATS);

        return uploadReportDataInSheet(reportData, currentReportSheetId);
    }

    /**
     * Updates Name of user in current spreadsheet (after repeated /reg usage)
     *
     * @param prevName previous Username
     * @param newName new Username
     * @param spreadsheetId id
     */
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

    /** Finding Spreadsheet of current month or Initializing new one
     *
     * @param timestamp time, when message was received
     * @param sheetType type of spreadsheet we're looking for
     * @return returns ID of found spreadsheet
     */
    public static String getCurrentSpreadsheet(int timestamp, SheetType sheetType) throws IOException, GeneralSecurityException {

        if (currentSheetsId.containsKey(sheetType)) {
            return currentSheetsId.get(sheetType);
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
            currentSheetName = prefixes.get(sheetType).concat("01.").concat(Integer.toString(Integer.parseInt(outputFmt.format(calendar.getTime())) + 1));
        }
        else {
            SimpleDateFormat outputFmt = new SimpleDateFormat("MM.yy");
            outputFmt.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
            currentSheetName = prefixes.get(sheetType).concat(outputFmt.format(calendar.getTime()));
        }

        for (File file : sheetsList.getFiles()) {
            if (file.getName().equals(currentSheetName)) {
                currentSheetsId.put(sheetType, file.getId());
                break;
            }
        }

        if (!currentSheetsId.containsKey(sheetType)) {
            currentSheetsId.put(sheetType, createNewSheet(currentSheetName, sheetType));
        }

        return currentSheetsId.get(sheetType);
    }

    /** Finding Name of User by it's ID
     *
     * @param userId id of the user rom message
     * @return returns Name, Surname & Last Name
     */
    public static @Nullable String getNameById(int userId) throws IOException, GeneralSecurityException {

        setIdNameFile();

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