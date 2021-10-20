package com.risuplabs.ureport_r4v.surveyor.data;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.risuplabs.ureport_r4v.di.SurveyorApplication;
import com.risuplabs.ureport_r4v.surveyor.engine.OrgAssets;
import com.risuplabs.ureport_r4v.surveyor.net.TembaException;
import com.risuplabs.ureport_r4v.surveyor.net.TembaService;
import com.risuplabs.ureport_r4v.surveyor.net.responses.Boundary;
import com.risuplabs.ureport_r4v.surveyor.net.responses.Field;
import com.risuplabs.ureport_r4v.surveyor.net.responses.Group;
import com.risuplabs.ureport_r4v.utils.AppConstant;
import com.risuplabs.ureport_r4v.utils.pref_manager.SharedPrefManager;
import com.risuplabs.ureport_r4v.utils.surveyor.JsonUtils;
import com.risuplabs.ureport_r4v.utils.surveyor.Logger;
import com.risuplabs.ureport_r4v.utils.surveyor.RawJson;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Org {

    /**
     * Contains the JSON representation of this org
     */
    public static final String DETAILS_FILE = "details.json";

    /**
     * Contains a goflow assets file with this org's flows, groups, fields etc
     */
    public static final String ASSETS_FILE = "assets.json";

    /**
     * Contains summaries of each flow available in this org
     */
    public static final String FLOWS_FILE = "flows.json";

    private String token;

    private String name;

    @SerializedName("primary_language")
    private String primaryLanguage;

    private String[] languages;

    private String timezone;

    private String country;

    @SerializedName("date_style")
    private String dateStyle;

    private boolean anon;

    private String legacySubmissionsDirectory;

    private transient File directory;

    public transient List<Flow> flows;

    public transient List<com.risuplabs.ureport_r4v.surveyor.net.responses.Flow> flowTitles;

    /**
     * Creates an new empty org
     *
     * @param directory the directory
     * @param token     the API token
     * @return the org
     */
    public static Org create(File directory, String name, String token) throws IOException {
        directory.mkdirs();

        Org org = new Org();
        org.name = name;
        org.token = token;
        org.directory = directory;
        org.flows = new ArrayList<>();
        org.legacySubmissionsDirectory = null;

        FileUtils.writeStringToFile(new File(directory, DETAILS_FILE + AppConstant.BRAZIL_ORG_ID), "{\"name\":\"" + name + "\",\"token\":\"" + token + "\"}");
        FileUtils.writeStringToFile(new File(directory, DETAILS_FILE + AppConstant.ECUADOR_ORG_ID), "{\"name\":\"" + name + "\",\"token\":\"" + token + "\"}");
        FileUtils.writeStringToFile(new File(directory, DETAILS_FILE + AppConstant.BOLIVIA_ORG_ID), "{\"name\":\"" + name + "\",\"token\":\"" + token + "\"}");
        FileUtils.writeStringToFile(new File(directory, FLOWS_FILE + AppConstant.BRAZIL_ORG_ID), "[]");
        FileUtils.writeStringToFile(new File(directory, FLOWS_FILE + AppConstant.ECUADOR_ORG_ID), "[]");
        FileUtils.writeStringToFile(new File(directory, FLOWS_FILE + AppConstant.BOLIVIA_ORG_ID), "[]");
        FileUtils.writeStringToFile(new File(directory, FLOWS_FILE + AppConstant.BRAZIL_ORG_ID+"poll"), "[]");
        FileUtils.writeStringToFile(new File(directory, FLOWS_FILE + AppConstant.BRAZIL_ORG_ID+"bot"), "[]");
        FileUtils.writeStringToFile(new File(directory, FLOWS_FILE + AppConstant.BRAZIL_ORG_ID+"reg"), "[]");
        FileUtils.writeStringToFile(new File(directory, FLOWS_FILE + AppConstant.ECUADOR_ORG_ID+"poll"), "[]");
        FileUtils.writeStringToFile(new File(directory, FLOWS_FILE + AppConstant.ECUADOR_ORG_ID+"bot"), "[]");
        FileUtils.writeStringToFile(new File(directory, FLOWS_FILE + AppConstant.ECUADOR_ORG_ID+"reg"), "[]");
        FileUtils.writeStringToFile(new File(directory, FLOWS_FILE + AppConstant.BOLIVIA_ORG_ID+"poll"), "[]");
        FileUtils.writeStringToFile(new File(directory, FLOWS_FILE + AppConstant.BOLIVIA_ORG_ID+"bot"), "[]");
        FileUtils.writeStringToFile(new File(directory, FLOWS_FILE + AppConstant.BOLIVIA_ORG_ID+"reg"), "[]");

        return org;
    }

    /**
     * Loads an org from a directory
     *
     * @param directory the directory
     * @return the org
     */
    static Org load(File directory, String types) throws IOException {
        if (!directory.exists() || !directory.isDirectory()) {
            throw new RuntimeException(directory.getPath() + " is not a valid org directory");
        }

        // read details.json
        String detailsJSON = FileUtils.readFileToString(new File(directory, DETAILS_FILE));
        Org org = JsonUtils.unmarshal(detailsJSON, Org.class);
        org.directory = directory;

        // read flows.json
        String flowsJson = FileUtils.readFileToString(new File(directory, FLOWS_FILE + SharedPrefManager.getORGID()+types));

        TypeToken type = new TypeToken<List<Flow>>() {
        };
        org.flows = JsonUtils.unmarshal(flowsJson, type);
        return org;
    }

    /**
     * Gets the UUID of this org (i.e. the name of its directory)
     *
     * @return the UUID
     */
    public String getUuid() {
        return directory.getName();
    }

    /**
     * Gets the directory of this org
     *
     * @return the directory
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * Gets the API token for this org
     *
     * @return the API token
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets the name of this org
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the country code of this org
     *
     * @return the country code
     */
    public String getCountry() {
        return country;
    }

    public String getPrimaryLanguage() {
        return primaryLanguage;
    }

    public String[] getLanguages() {
        return languages;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getDateStyle() {
        return dateStyle;
    }

    public boolean isAnon() {
        return anon;
    }

    public List<Flow> getFlows() {
        return flows;
    }

    /**
     * Gets the flow with the given UUID
     *
     * @param uuid the flow UUID
     * @return the flow or null if no such flow exists
     */
    public Flow getFlow(String uuid) {
        for (Flow flow : flows) {
            if (flow.getUuid().equals(uuid)) {
                return flow;
            }
        }
        return null;
    }

    /**
     * Gets whether this org has downloaded assets
     *
     * @return true if org has assets
     */
    public boolean hasAssets(String type) {
        return new File(directory, ASSETS_FILE + SharedPrefManager.getORGID()+type).exists();
    }

    /**
     * Gets this org's downloaded assets
     *
     * @return the assets JSON
     */
    public String getAssets(String type) throws IOException {
        return FileUtils.readFileToString(new File(directory, ASSETS_FILE + SharedPrefManager.getORGID()+type));
    }

    /**
     * Refreshes this org from RapidPro
     */
    public void refresh(boolean includeAssets, RefreshProgress progress) throws TembaException, IOException {
        TembaService svc = SurveyorApplication.get().getTembaService();
        com.risuplabs.ureport_r4v.surveyor.net.responses.Org apiOrg = svc.getOrg(this.token);

        this.name = apiOrg.getName();
        this.primaryLanguage = apiOrg.getPrimaryLanguage();
        this.languages = apiOrg.getLanguages();
        this.timezone = apiOrg.getTimezone();
        this.country = apiOrg.getCountry();
        this.dateStyle = apiOrg.getDateStyle();
        this.anon = apiOrg.isAnon();
        this.save();

        if (progress != null) {
            progress.reportProgress(10);
        }

        if (includeAssets) {
            refreshAssets(progress);
        }
    }

    public void download(boolean includeAssets, RefreshProgress progress, List<com.risuplabs.ureport_r4v.surveyor.net.responses.Flow> flows,String type) throws TembaException, IOException {
        TembaService svc = SurveyorApplication.get().getTembaService();
        com.risuplabs.ureport_r4v.surveyor.net.responses.Org apiOrg = svc.getOrg(this.token);

        this.name = apiOrg.getName();
        this.primaryLanguage = apiOrg.getPrimaryLanguage();
        this.languages = apiOrg.getLanguages();
        this.timezone = apiOrg.getTimezone();
        this.country = apiOrg.getCountry();
        this.dateStyle = apiOrg.getDateStyle();
        this.anon = apiOrg.isAnon();
        this.save();

        if (progress != null) {
            progress.reportProgress(10);
        }

        if (includeAssets) {
            downloadAssets(progress, flows, type);
        }
    }

    public void save() throws IOException {
        // (re)write org fields to details.json
        String detailsJSON = JsonUtils.marshal(this);
        FileUtils.writeStringToFile(new File(directory, DETAILS_FILE), detailsJSON);
    }

    private void refreshAssets(RefreshProgress progress) throws TembaException, IOException {


        List<com.risuplabs.ureport_r4v.surveyor.net.responses.Flow> flows = SurveyorApplication.get().getTembaService().getFlows(getToken());

        progress.reportProgress(50);

        List<com.risuplabs.ureport_r4v.surveyor.net.responses.Flow> filtered_flows = SurveyorApplication.get().getTembaService().get_filtered_flow(flows);

        this.flowTitles = filtered_flows;

        progress.reportProgress(100);

    }

    public void downloadAssets(RefreshProgress progress, List<com.risuplabs.ureport_r4v.surveyor.net.responses.Flow> filtered_flows, String type) throws TembaException, IOException {

        List<Field> fields = SurveyorApplication.get().getTembaService().getFields(getToken());

        progress.reportProgress(20);

        List<Group> groups = SurveyorApplication.get().getTembaService().getGroups(getToken());

        progress.reportProgress(40);

        List<RawJson> definitions = SurveyorApplication.get().getTembaService().getDefinitions(getToken(), filtered_flows);

        progress.reportProgress(60);

        List<Boundary> boundaries = SurveyorApplication.get().getTembaService().getBoundaries(getToken());

        progress.reportProgress(70);

        OrgAssets assets = OrgAssets.fromTemba(fields, groups, boundaries, definitions);
        String assetsJSON = JsonUtils.marshal(assets);

        FileUtils.writeStringToFile(new File(directory, ASSETS_FILE + SharedPrefManager.getORGID()+type), assetsJSON);

        progress.reportProgress(80);

        // update the flow summaries
        this.flows.clear();
        this.flows.addAll(assets.getFlows());

        // and write that to flows.json as well
        String summariesJSON = JsonUtils.marshal(this.flows);
        FileUtils.writeStringToFile(new File(directory, FLOWS_FILE + SharedPrefManager.getORGID()), summariesJSON);
        FileUtils.writeStringToFile(new File(directory, FLOWS_FILE + SharedPrefManager.getORGID()+type), summariesJSON);

        progress.reportProgress(100);

        Logger.d("Refreshed assets for org " + getUuid() + " (flows=" + flows.size() + ", fields=" + fields.size() + ", groups=" + groups.size() + ")");
    }

    public interface RefreshProgress {
        void reportProgress(int percent);
    }
}
