package co.infinum.princeofversions.tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Map;

import co.infinum.princeofversions.BuildConfig;
import co.infinum.princeofversions.LoaderFactory;
import co.infinum.princeofversions.PrinceOfVersions;
import co.infinum.princeofversions.UpdateConfigLoader;
import co.infinum.princeofversions.callbacks.UpdaterCallback;
import co.infinum.princeofversions.common.ErrorCode;
import co.infinum.princeofversions.helpers.ContextHelper;
import co.infinum.princeofversions.helpers.parsers.JsonVersionConfigParser;
import co.infinum.princeofversions.interfaces.SdkVersionProvider;
import co.infinum.princeofversions.interfaces.VersionRepository;
import co.infinum.princeofversions.interfaces.VersionVerifier;
import co.infinum.princeofversions.interfaces.VersionVerifierFactory;
import co.infinum.princeofversions.loaders.ResourceFileLoader;
import co.infinum.princeofversions.util.SdkVersionProviderMock;
import co.infinum.princeofversions.verifiers.SingleThreadVersionVerifier;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Matchers.eq;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class PrinceOfVersionsTest {

    private UpdaterCallback callback;

    private VersionVerifier versionVerifier;

    private VersionVerifierFactory provider;

    private VersionRepository repository;

    @Before
    public void setUp() {
        callback = Mockito.mock(UpdaterCallback.class);
        provider = Mockito.mock(VersionVerifierFactory.class);
        repository = Mockito.mock(VersionRepository.class);
        Mockito.when(repository.getLastVersionName(Mockito.anyString())).thenReturn(null);
        Mockito.doNothing().when(repository).setLastVersionName(Mockito.anyString());
    }

    private Context setupContext(String versionName) throws PackageManager.NameNotFoundException {
        Context context = Mockito.mock(Context.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(context.getPackageName()).thenReturn("name");
        PackageInfo packageInfo = Mockito.mock(PackageInfo.class);
        packageInfo.versionName = versionName;
        Mockito.when(context.getPackageManager().getPackageInfo("name", 0)).thenReturn(packageInfo);
        versionVerifier = new SingleThreadVersionVerifier(new JsonVersionConfigParser(ContextHelper.getAppVersion(context)));
        Mockito.when(provider.newInstance()).thenReturn(versionVerifier);
        return context;
    }

    private SdkVersionProvider setupSdkInt(int sdkInt) {
        return new SdkVersionProviderMock(sdkInt);
    }

    @Test
    public void testCheckingValidContentNoNotification() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_no_notification.json");
            }
        }, callback);
        Mockito.verify(callback, Mockito.times(1)).onNewUpdate(eq("2.4.5"), eq(false), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(null);
    }

    @Test
    public void testCheckingValidContentNotificationAlways() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_notification_always.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(1)).onNewUpdate(eq("2.4.5"), eq(false), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(null);
    }

    @Test
    public void testCheckingValidContentOnlyMinVersion() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_only_min_version.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), Mockito.anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
        Mockito.verify(callback, Mockito.times(1)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
    }

    @Test
    public void testCheckingValidContentWithoutCodes() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_full.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(1)).onNewUpdate(eq("2.4.5"), eq(false), Matchers.<Map<String, String>>any());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(null);
    }

    @Test
    public void testCheckingContentJSONWhenCurrentIsGreaterThanMinAndLessThanOptional() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_full.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(1)).onNewUpdate(eq("2.4.5"), eq(false), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(null);
    }

    @Test
    public void testCheckingContentJSONWhenCurrentIsLessThanMinAndLessThanOptional() throws PackageManager.NameNotFoundException {
        Context context = setupContext("1.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_full.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(1)).onNewUpdate(eq("2.4.5"), eq(true), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(null);
    }

    @Test
    public void testCheckingContentJSONWhenCurrentIsEqualToMinAndLessThanOptional() throws PackageManager.NameNotFoundException {
        Context context = setupContext("1.2.3");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_full.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(1)).onNewUpdate(eq("2.4.5"), eq(false), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(null);
    }

    @Test
    public void testCheckingContentJSONWhenCurrentIsGreaterThanMinAndEqualToOptional() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.4.5");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_full.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), Mockito.anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
        Mockito.verify(callback, Mockito.times(1)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
    }

    @Test
    public void testCheckingContentJSONWhenCurrentIsGreaterThanMinAndGreaterThanOptional() throws PackageManager.NameNotFoundException {
        Context context = setupContext("3.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_full.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), Mockito.anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
        Mockito.verify(callback, Mockito.times(1)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
    }

    @Test
    public void testCheckingInvalidContentWithInvalidVersion() throws PackageManager.NameNotFoundException {
        Context context = setupContext("3.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("invalid_update_invalid_version.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), Mockito.anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(1)).onError(ErrorCode.WRONG_VERSION);
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(null);
        Mockito.verifyNoMoreInteractions(callback);
    }

    @Test
    public void testCheckingInvalidContentNoAndroidKey() throws PackageManager.NameNotFoundException {
        Context context = setupContext("3.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("invalid_update_no_android.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), Mockito.anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(1)).onError(ErrorCode.WRONG_VERSION);
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(null);
    }

    @Test
    public void testCheckingInvalidContentNoJSON() throws PackageManager.NameNotFoundException {
        Context context = setupContext("3.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("invalid_update_no_json.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), Mockito.anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(1)).onError(ErrorCode.WRONG_VERSION);
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(null);
    }

    @Test
    public void testCheckingValidContentWithAlwaysNotification() throws PackageManager.NameNotFoundException {
        Context context = setupContext("3.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_notification_always.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), Mockito.anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.WRONG_VERSION);
        Mockito.verify(callback, Mockito.times(1)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
    }

    @Test
    public void testCheckingValidContentWithOnlyMinVersion() throws PackageManager.NameNotFoundException {
        Context context = setupContext("3.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_only_min_version.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), Mockito.anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.WRONG_VERSION);
        Mockito.verify(callback, Mockito.times(1)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
    }

    @Test
    public void testCheckingWhenVersionIsAlreadyNotified() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.0.0");
        VersionRepository repo = Mockito.mock(VersionRepository.class);
        Mockito.when(repo.getLastVersionName(null)).thenReturn("2.4.5");
        Mockito.when(repo.getLastVersionName(Mockito.anyString())).thenReturn("2.4.5");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repo);
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_full.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), Mockito.anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.WRONG_VERSION);
        Mockito.verify(callback, Mockito.times(1)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
    }

    @Test
    public void testCheckingWhenCurrentAppVersionIsInvalid() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_full.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), Mockito.anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(1)).onError(ErrorCode.WRONG_VERSION);
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
    }

    @Test
    public void testCheckingWhenUpdateShouldBeMade() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_no_sdk_values.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(1))
                .onNewUpdate(eq("2.4.5"), eq(false), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(null);
    }

    @Test
    public void testCheckingUpdateWithFullSdkValues() throws PackageManager.NameNotFoundException {
        Context context = setupContext("1.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_with_sdk_values.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(1))
                .onNewUpdate(Mockito.anyString(), eq(true), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    public void testCheckingUpdateWithASingleSdkValue() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_with_single_sdk_value.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(1))
                .onNewUpdate(Mockito.anyString(), eq(false), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
    }

    @Test
    public void testCheckingUpdateWithHugeSdkValues() throws PackageManager.NameNotFoundException {
        Context context = setupContext("1.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_with_huge_sdk_values.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
        Mockito.verify(callback, Mockito.times(1)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
    }

    @Test
    public void testCheckingUpdateWithDowngradingSdkValues() throws PackageManager.NameNotFoundException {
        Context context = setupContext("1.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_with_downgrading_sdk_values.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(1))
                .onNewUpdate(Mockito.anyString(), eq(true), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
    }

    @Test
    public void testCheckingOptionalUpdateWithDowngradingSdkValues() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.4.1");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_with_downgrading_sdk_values.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(1))
                .onNewUpdate(Mockito.anyString(), eq(false), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    public void testCheckingOptionalUpdateWithHugeSdkValues() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.4.1");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_with_huge_sdk_values.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(1)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    public void testCheckingOptionalUpdateWithSingleSdkValue() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.4.1");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_with_single_sdk_value.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(1))
                .onNewUpdate(Mockito.anyString(), eq(false), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    public void testCheckingOptionalUpdateWithSingleSdkValueAndHigherInitialVersion() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.4.4");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_with_single_sdk_value.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(1))
                .onNewUpdate(Mockito.anyString(), eq(false), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    public void testCheckingMandatoryUpdateWithSdkValues() throws PackageManager.NameNotFoundException {
        Context context = setupContext("1.4.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_mandatory_update.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(1))
                .onNewUpdate(Mockito.anyString(), eq(true), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    public void testCheckingOptionalUpdateWithSdkValuesAndTheSameVersions() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.4.5");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_full_b_with_sdk_values.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(1)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    public void testCheckingOptionalUpdateWithSdkValuesAndWithDifferentVersions() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.4.4");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_full_b_with_sdk_values.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(1))
                .onNewUpdate(Mockito.anyString(), eq(false), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    public void testCheckingOptionalUpdateWithSdkValuesAndIncreaseInMinor() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.3.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_full_with_sdk_values.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(1)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    public void testCheckingOptionalUpdateWithHighSdkValuesAndIncreaseInMinor() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.3.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(16));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_with_big_increase_in_sdk_values.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(1)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    public void testCheckingMandatoryUpdateWithDeviceThatHasVeryLowMinSdk() throws PackageManager.NameNotFoundException {
        Context context = setupContext("1.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(12));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_with_same_sdk_values.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(1)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    public void testCheckingMandatoryUpdateWithUnavailableOptionalUpdate() throws PackageManager.NameNotFoundException {
        Context context = setupContext("1.2.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(14));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_with_small_sdk_values.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(1))
                .onNewUpdate(Mockito.anyString(), eq(true), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    public void testCheckingOptionalUpdateWithBigMinimumVersionMinSdk() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.0.0");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(23));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_with_big_minimum_version_min_sdk.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(1))
                .onNewUpdate(Mockito.anyString(), eq(false), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    public void testCheckingOptionalUpdateWhenUserIsUpToDate() throws PackageManager.NameNotFoundException {
        Context context = setupContext("2.1.1");
        PrinceOfVersions updater = new PrinceOfVersions(context, provider, repository, setupSdkInt(20));
        updater.checkForUpdates(new LoaderFactory() {
            @Override
            public UpdateConfigLoader newInstance() {
                return new ResourceFileLoader("valid_update_with_big_minimum_version_min_sdk.json");
            }
        }, callback);

        Mockito.verify(callback, Mockito.times(0))
                .onNewUpdate(Mockito.anyString(), anyBoolean(), ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(1)).onNoUpdate(ArgumentMatchers.<String, String>anyMap());
        Mockito.verify(callback, Mockito.times(0)).onError(ErrorCode.UNKNOWN_ERROR);
    }

}