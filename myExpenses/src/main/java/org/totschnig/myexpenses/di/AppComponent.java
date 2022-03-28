package org.totschnig.myexpenses.di;

import android.content.Context;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.squareup.picasso.Picasso;

import org.totschnig.myexpenses.MyApplication;
import org.totschnig.myexpenses.activity.BaseActivity;
import org.totschnig.myexpenses.activity.BaseMyExpenses;
import org.totschnig.myexpenses.activity.DebtOverview;
import org.totschnig.myexpenses.activity.ExpenseEdit;
import org.totschnig.myexpenses.activity.ProtectedFragmentActivity;
import org.totschnig.myexpenses.db2.Repository;
import org.totschnig.myexpenses.delegate.CategoryDelegate;
import org.totschnig.myexpenses.delegate.SplitDelegate;
import org.totschnig.myexpenses.delegate.TransferDelegate;
import org.totschnig.myexpenses.dialog.AmountFilterDialog;
import org.totschnig.myexpenses.dialog.BaseDialogFragment;
import org.totschnig.myexpenses.dialog.ContribDialogFragment;
import org.totschnig.myexpenses.dialog.DebtDetailsDialogFragment;
import org.totschnig.myexpenses.dialog.DonateDialogFragment;
import org.totschnig.myexpenses.dialog.EditCurrencyDialog;
import org.totschnig.myexpenses.dialog.ExtendProLicenceDialogFragment;
import org.totschnig.myexpenses.dialog.RemindRateDialogFragment;
import org.totschnig.myexpenses.dialog.TransactionDetailFragment;
import org.totschnig.myexpenses.dialog.TransactionListDialogFragment;
import org.totschnig.myexpenses.dialog.VersionDialogFragment;
import org.totschnig.myexpenses.dialog.select.SelectFromTableDialogFragment;
import org.totschnig.myexpenses.export.pdf.PdfPrinter;
import org.totschnig.myexpenses.feature.FeatureManager;
import org.totschnig.myexpenses.feature.OcrFeature;
import org.totschnig.myexpenses.fragment.BaseSettingsFragment;
import org.totschnig.myexpenses.fragment.BaseTransactionList;
import org.totschnig.myexpenses.fragment.BudgetFragment;
import org.totschnig.myexpenses.fragment.BudgetList;
import org.totschnig.myexpenses.fragment.CsvImportDataFragment;
import org.totschnig.myexpenses.fragment.CsvImportParseFragment;
import org.totschnig.myexpenses.fragment.CurrencyList;
import org.totschnig.myexpenses.fragment.DistributionFragment;
import org.totschnig.myexpenses.fragment.HistoryChart;
import org.totschnig.myexpenses.fragment.OnBoardingPrivacyFragment;
import org.totschnig.myexpenses.fragment.OnboardingDataFragment;
import org.totschnig.myexpenses.fragment.OnboardingUiFragment;
import org.totschnig.myexpenses.fragment.PartiesList;
import org.totschnig.myexpenses.fragment.PlannerFragment;
import org.totschnig.myexpenses.fragment.SettingsFragment;
import org.totschnig.myexpenses.fragment.SplitPartList;
import org.totschnig.myexpenses.fragment.StaleImagesList;
import org.totschnig.myexpenses.fragment.SyncBackendList;
import org.totschnig.myexpenses.fragment.TemplatesList;
import org.totschnig.myexpenses.model.CurrencyContext;
import org.totschnig.myexpenses.preference.PrefHandler;
import org.totschnig.myexpenses.provider.TransactionProvider;
import org.totschnig.myexpenses.retrofit.ExchangeRateService;
import org.totschnig.myexpenses.service.AutoBackupService;
import org.totschnig.myexpenses.service.PlanExecutor;
import org.totschnig.myexpenses.service.SyncNotificationDismissHandler;
import org.totschnig.myexpenses.task.ExportTask;
import org.totschnig.myexpenses.task.LicenceApiTask;
import org.totschnig.myexpenses.util.CurrencyFormatter;
import org.totschnig.myexpenses.util.ads.BaseAdHandler;
import org.totschnig.myexpenses.util.crashreporting.CrashHandler;
import org.totschnig.myexpenses.util.licence.LicenceHandler;
import org.totschnig.myexpenses.util.locale.UserLocaleProvider;
import org.totschnig.myexpenses.util.tracking.Tracker;
import org.totschnig.myexpenses.viewmodel.BackupViewModel;
import org.totschnig.myexpenses.viewmodel.BudgetViewModel;
import org.totschnig.myexpenses.viewmodel.ContentResolvingAndroidViewModel;
import org.totschnig.myexpenses.viewmodel.CurrencyViewModel;
import org.totschnig.myexpenses.viewmodel.DebtViewModel;
import org.totschnig.myexpenses.viewmodel.DistributionViewModel;
import org.totschnig.myexpenses.viewmodel.EditCurrencyViewModel;
import org.totschnig.myexpenses.viewmodel.ExchangeRateViewModel;
import org.totschnig.myexpenses.viewmodel.FeatureViewModel;
import org.totschnig.myexpenses.viewmodel.OcrViewModel;
import org.totschnig.myexpenses.viewmodel.RoadmapViewModel;
import org.totschnig.myexpenses.viewmodel.UpgradeHandlerViewModel;
import org.totschnig.myexpenses.widget.AbstractWidget;
import org.totschnig.myexpenses.widget.TemplateRemoteViewsFactory;

import java.time.LocalDate;
import java.util.Locale;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import okhttp3.OkHttpClient;

@Singleton
@Component(modules = {AppModule.class, UiModule.class, NetworkModule.class, LicenceModule.class,
    DataModule.class, CoroutineModule.class, ViewModelModule.class, FeatureModule.class,
    CrashHandlerModule.class})
public interface AppComponent {
  String USER_COUNTRY = "userCountry";
  String DATABASE_NAME = "databaseName";

  @Component.Builder
  interface Builder {
    @BindsInstance
    Builder systemLocale(Locale locale);

    @BindsInstance
    Builder applicationContext(MyApplication applicationContext);

    Builder licenceModule(LicenceModule licenceModule);

    Builder coroutineModule(CoroutineModule coroutineModule);

    Builder viewModelModule(ViewModelModule viewModelModule);

    Builder dataModule(DataModule dataModule);

    Builder featureModule(FeatureModule featureModule);

    Builder crashHandlerModule(CrashHandlerModule crashHandlerModule);

    Builder uiModule(UiModule uiModule);

    Builder networkModule(NetworkModule networkModule);

    AppComponent build();
  }

  CrashHandler crashHandler();

  Tracker tracker();

  PrefHandler prefHandler();

  LicenceHandler licenceHandler();

  @Named(USER_COUNTRY)
  String userCountry();

  CurrencyContext currencyContext();

  CurrencyFormatter currencyFormatter();

  ExchangeRateService exchangeRateService();

  UserLocaleProvider userLocaleProvider();

  Picasso picasso();

  Context context();

  Repository repository();

  JsonDeserializer<LocalDate> localDateJsonDeserializer();

  Gson gson();

  @Nullable
  OcrFeature ocrFeature();

  OkHttpClient.Builder okHttpClientBuilder();

  FeatureManager featureManager();

  void inject(MyApplication application);

  void inject(ExpenseEdit expenseEdit);

  void inject(BaseMyExpenses myExpenses);

  void inject(DebtOverview debtOverview);

  void inject(ProtectedFragmentActivity protectedFragmentActivity);

  void inject(TransactionDetailFragment transactionDetailFragment);

  void inject(StaleImagesList staleImagesList);

  void inject(PdfPrinter pdfPrinter);

  void inject(TemplatesList templatesList);

  void inject(BaseTransactionList transactionList);

  void inject(SplitPartList splitPartList);

  void inject(TransactionListDialogFragment transactionListDialogFragment);

  void inject(BudgetFragment budgetFragment);

  void inject(DistributionFragment distributionFragment);

  void inject(BaseAdHandler adHandler);

  void inject(LicenceApiTask licenceApiTask);

  void inject(SettingsFragment settingsFragment);

  void inject(ContribDialogFragment contribDialogFragment);

  void inject(RoadmapViewModel roadmapViewModel);

  void inject(HistoryChart historyChart);

  void inject(DonateDialogFragment donateDialogFragment);

  void inject(AutoBackupService autoBackupService);

  void inject(SyncNotificationDismissHandler syncNotificationDismissHandler);

  void inject(SyncBackendList syncBackendList);

  void inject(AmountFilterDialog amountFilterDialog);

  void inject(CurrencyList currencyList);

  void inject(EditCurrencyDialog editCurrencyDialog);

  void inject(TransactionProvider transactionProvider);

  void inject(OnboardingDataFragment onboardingDataFragment);

  void inject(EditCurrencyViewModel editCurrencyViewModel);

  void inject(PlanExecutor planExecutor);

  void inject(BudgetViewModel budgetViewModel);

  void inject(ContentResolvingAndroidViewModel contentResolvingAndroidViewModel);

  void inject(CurrencyViewModel contentResolvingAndroidViewModel);

  void inject(UpgradeHandlerViewModel upgradeHandlerViewModel);

  void inject(SelectFromTableDialogFragment selectFromTableDialogFragment);

  void inject(BudgetList budgetList);

  void inject(OnBoardingPrivacyFragment onBoardingPrivacyFragment);

  void inject(RemindRateDialogFragment remindRateDialogFragment);

  void inject(TemplateRemoteViewsFactory templateRemoteViewsFactory);

  void inject(AbstractWidget abstractWidget);

  void inject(ExchangeRateViewModel exchangeRateViewModel);

  void inject(OnboardingUiFragment onboardingUiFragment);

  void inject(PlannerFragment.PlanInstanceViewHolder planInstanceViewHolder);

  void inject(BaseSettingsFragment baseSettingsFragment);

  void inject(ExtendProLicenceDialogFragment extendProLicenceDialogFragment);

  void inject(VersionDialogFragment versionDialogFragment);

  void inject(BaseActivity baseActivity);

  void inject(OcrViewModel ocrViewModel);

  void inject(BaseDialogFragment baseDialogFragment);

  void inject(CsvImportParseFragment csvImportParseFragment);

  void inject(FeatureViewModel featureViewModel);

  void inject(CsvImportDataFragment csvImportDataFragment);

  void inject(BackupViewModel backupViewModel);

  void inject(DebtViewModel debtViewModel);

  void inject(DebtDetailsDialogFragment debtDetailsDialogFragment);

  void inject(CategoryDelegate transactionDelegate);

  void inject(SplitDelegate transactionDelegate);

  void inject(TransferDelegate transactionDelegate);

  void inject(ExportTask exportTask);

  void inject(PartiesList partiesList);

  void inject(DistributionViewModel distributionViewModel);
}
