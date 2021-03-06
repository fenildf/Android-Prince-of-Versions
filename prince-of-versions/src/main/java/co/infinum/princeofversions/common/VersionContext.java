package co.infinum.princeofversions.common;

import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Holder for version information provided by VersionVerifier.
 * <p>
 * Holder contains information about current application version, minimum application version provided by update configuration and
 * optionally optional update information. Apart from that, holder also contains information is current version less than minimum or
 * optional.
 * </p>
 */
public class VersionContext {

    /**
     * Current version of application.
     */
    private Version currentVersion;

    /**
     * Minimum version specified by update configuration.
     */
    @Nullable
    private Version minimumVersion;

    /**
     * Optional configuration specified by update configuration.
     */
    @Nullable
    private UpdateContext optionalUpdate;

    /**
     * Flag determines if current application version is less than minimum version specified by update configuration.
     * Flag has value true if current application version is less than minimum version specified by update configuration, false otherwise.
     */
    private boolean isCurrentLessThanMinimum;

    /**
     * Flag determines if current application version is less than optional version specified by update configuration.
     * Flag has value true if current application version is less than optional version specified by update configuration, false otherwise.
     */
    private boolean isCurrentLessThanOptional;

    /**
     * Metadata sent with the request.
     */
    private Map<String, String> metadata;

    /**
     * This field represent the minSdk value of the minimum version of the app from the JSON config file.
     */
    private int minimumVersionMinSdk;

    /**
     * Creates a new holder with specified current, minimum and optional update version and corresponding flags.
     *
     * @param currentVersion            Current application version.
     * @param minimumVersion            Minimum version specified by update configuration.
     * @param isCurrentLessThanMinimum  Flag for setting if current version is less than minimum.
     * @param optionalUpdate            Optional version specified by update configuration.
     * @param isCurrentLessThanOptional Flag for setting if current version is less than optional.
     * @param minimumVersionMinSdk          Minimum app version minSdk value.
     */
    public VersionContext(Version currentVersion, @Nullable Version minimumVersion, boolean isCurrentLessThanMinimum,
            UpdateContext optionalUpdate, boolean isCurrentLessThanOptional, int minimumVersionMinSdk) {
        this.currentVersion = currentVersion;
        this.minimumVersion = minimumVersion;
        this.optionalUpdate = optionalUpdate;
        this.isCurrentLessThanMinimum = isCurrentLessThanMinimum;
        this.isCurrentLessThanOptional = isCurrentLessThanOptional;
        this.minimumVersionMinSdk = minimumVersionMinSdk;
    }

    /**
     * Creates a new holder with specified current and minimum version and corresponding flag.
     *
     * @param currentVersion           Current application version.
     * @param minimumVersion           Minimum version specified by update configuration.
     * @param isCurrentLessThanMinimum Flag for setting if current version is less than minimum.
     */
    public VersionContext(Version currentVersion, @Nullable Version minimumVersion, boolean isCurrentLessThanMinimum) {
        this(currentVersion, minimumVersion, isCurrentLessThanMinimum, null, false, UpdateContext.DEFAULT_MIN_SDK_VALUE);
    }

    /**
     * Method setting optional update part of this holder with optional update context and corresponding flag.
     *
     * @param optionalUpdate            Optional update part of this holder.
     * @param isCurrentLessThanOptional Flag for setting if current version is less than optional.
     */
    public void setOptionalUpdate(UpdateContext optionalUpdate, boolean isCurrentLessThanOptional) {
        this.optionalUpdate = optionalUpdate;
        this.isCurrentLessThanOptional = isCurrentLessThanOptional;
    }

    /**
     * Places the metadata container into this holder.
     *
     * @param metadata metadata do be added to this holder
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    /**
     * Determines if this holder contains optional update information.
     *
     * @return true if holder has optional update information, false otherwise.
     */
    public boolean hasOptionalUpdate() {
        return optionalUpdate != null;
    }

    /**
     * Determines if this holder contains current version less than minimum.
     *
     * @return true if holder contains current version less than minimum, false otherwise.
     */
    public boolean isCurrentLessThanMinimum() {
        return isCurrentLessThanMinimum;
    }

    /**
     * Determines if this holder contains current version less than optional.
     *
     * @return true if holder contains current version less than optional, false otherwise.
     */
    public boolean isCurrentLessThanOptional() {
        return hasOptionalUpdate() && isCurrentLessThanOptional;
    }

    /**
     * Provides current version stored in this holder.
     *
     * @return Current version from this holder.
     */
    public Version getCurrentVersion() {
        return currentVersion;
    }

    /**
     * Provides minimum version stored in this holder.
     *
     * @return Minimum version from this holder.
     */
    @Nullable
    public Version getMinimumVersion() {
        return minimumVersion;
    }

    /**
     * Provides optional update subholder stored in this holder.
     *
     * @return Optional update subholder from this holder.
     */
    public UpdateContext getOptionalUpdate() {
        return optionalUpdate;
    }

    /**
     * Provides the metadata stored in this holder.
     *
     * @return metadata stored in this holder
     */
    public Map<String, String> getMetadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        return metadata;
    }

    /**
     * Provides last MinSdk of this holder
     *
     * @return last minSdk value as an integer.
     */
    public int getMinimumVersionMinSdk() {
        return minimumVersionMinSdk;
    }

    /**
     * Holder for specific Version determined by version string and version code.
     */
    public static class Version {

        /**
         * String representation of version.
         */
        private String versionString;

        /**
         * Creates a new holder for specific version from version string.
         *
         * @param versionString Version string.
         */
        public Version(String versionString) {
            this.versionString = versionString;
        }

        /**
         * Provides version string of this holder.
         *
         * @return Version string.
         */
        public String getVersionString() {
            return versionString;
        }

    }

    /**
     * Holder for optional update part of update configuration determined by optional update version and optionally notification type.
     */
    public static class UpdateContext {

        /**
         * Default notification type when creating holder without it.
         */
        public static final String DEFAULT_NOTIFICATION_TYPE = "ALWAYS";

        /**
         * If minSdk values are not passed through the parser we assume they are not sent from the API then setting these values to 0
         */
        public static final int DEFAULT_MIN_SDK_VALUE = 0;

        /**
         * Optional update version of this holder.
         */
        private Version version;

        /**
         * Notification type of this holder.
         */
        private String notificationType;

        /**
         * New minSdk value after the update
         */
        private int newMinSdk;

        /**
         * Creates a new holder from specific optional update data including the new minSdk value and old minSdk
         *
         * @param version          Optional update version.
         * @param notificationType Notification type string.
         * @param newMinSdk        new minSdk value as int.
         */
        public UpdateContext(Version version, @Nullable String notificationType, int newMinSdk) {
            this.version = version;
            this.notificationType = notificationType == null ? DEFAULT_NOTIFICATION_TYPE : notificationType;
            this.newMinSdk = newMinSdk;
        }

        /**
         * Creates a new holder from specific optional update version and notification type.
         *
         * @param version          Optional update version.
         * @param notificationType Notification type string.
         */
        public UpdateContext(Version version, @Nullable String notificationType) {
            this(version, notificationType, DEFAULT_MIN_SDK_VALUE);
        }

        /**
         * Creates a new holder only from optional update version.
         *
         * @param version Optional update version.
         */
        public UpdateContext(Version version) {
            this(version, DEFAULT_NOTIFICATION_TYPE);
        }

        /**
         * Provides optional update version of this holder.
         *
         * @return Optional update version of this holder.
         */
        public Version getVersion() {
            return version;
        }

        /**
         * Method sets optional update version of this holder.
         *
         * @param version Optional update version.
         */
        public void setVersion(Version version) {
            this.version = version;
        }

        /**
         * Provides notification type of this holder.
         *
         * @return Notification type of this holder.
         */
        public String getNotificationType() {
            return notificationType;
        }

        /**
         * Method sets notification type of this holder.
         *
         * @param notificationType Notification type.
         */
        public void setNotificationType(String notificationType) {
            this.notificationType = notificationType;
        }

        /**
         * Provides new MinSdk of this holder
         *
         * @return new minSdk value as an integer.
         */
        public int getNewMinSdk() {
            return newMinSdk;
        }
    }

}
