package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.Manifest;
import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.InitDatabaseHelper;
import nl.erikduisters.pathfinder.data.local.GpsManager;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.data.usecase.InitDatabase;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.RequestRuntimePermissionState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowFatalErrorMessageState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

/**
 * Created by Erik Duisters on 08-06-2018.
 */

//TODO: MainActivity has changed a lot, make sure tests still cover everything
@RunWith(JUnit4.class)
public class MainActivityViewModelTest {
    @Mock
    private Observer<MainActivityViewState> observer;
    @Mock
    private InitDatabaseHelper initDatabaseHelper;
    @Mock
    private GpsManager gpsManager;
    @Mock
    private PreferenceManager preferenceManager;

    private MainActivityViewModel viewModel;

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        viewModel = new MainActivityViewModel(initDatabaseHelper, gpsManager, preferenceManager);
    }

    @Test
    public void whenCreated_startsWithInitDatabaseState() {
        LiveData<MainActivityViewState> liveData = viewModel.getMainActivityViewStateObservable();
        liveData.observeForever(observer);

        verify(observer).onChanged(ArgumentMatchers.isA(MainActivityViewState.InitDatabaseState.class));
    }

    @Test
    public void whenOnDatabaseInitializationProgressCalled_resultsInInitDatabaseState() {
        LiveData<MainActivityViewState> liveData = viewModel.getMainActivityViewStateObservable();

        InitDatabase.Progress progress = new InitDatabase.Progress(25, R.string.migration_1_to_2);
        viewModel.onDatabaseInitializationProgress(progress);

        assert(liveData.getValue() instanceof MainActivityViewState.InitDatabaseState);
        MainActivityViewState.InitDatabaseState state = (MainActivityViewState.InitDatabaseState) liveData.getValue();
        assertEquals(progress, state.progress);
    }

    @Test
    public void whenOnDatabaseInitializationCompleteCalled_resultsInInitStorageViewState() {
        LiveData<MainActivityViewState> liveData = viewModel.getMainActivityViewStateObservable();

        viewModel.onDatabaseInitializationComplete();

        assert(liveData.getValue() instanceof MainActivityViewState.InitStorageViewState);
    }

    @Test
    public void whenOnDatabaseInitializationErrorCalled_resultsInShowFatalErrorMessageState() {
        LiveData<MainActivityViewState> liveData = viewModel.getMainActivityViewStateObservable();

        Throwable throwable = new IllegalStateException("This cannot be");
        viewModel.onDatabaseInitializationError(throwable);

        assert(liveData.getValue() instanceof ShowFatalErrorMessageState);
        ShowFatalErrorMessageState state = (ShowFatalErrorMessageState) liveData.getValue();
        assertEquals(throwable, state.throwable);
    }

    /* TODO: Make this work again
    @Test
    public void whenOnFatalErrorMessageDismissedCalledWithoutThrowable_resultsInFinishedState() {
        LiveData<MainActivityViewState> liveData = viewModel.getMainActivityViewStateObservable();

        MessageWithTitle message = new MessageWithTitle(R.string.fatal_error, R.string.init_database_failed);
        viewModel.handleFatalError(message, null);
        viewModel.onFatalErrorMessageDismissed();

        assert(liveData.getValue() instanceof FinishState);
    } */

    /* TODO: Make this work again
    @Test
    public void whenOnFatalErrorMessageDismissedCalledWithThrowable_throwsException() {
        thrown.expect(RuntimeException.class);

        LiveData<MainActivityViewState> liveData = viewModel.getMainActivityViewStateObservable();

        MessageWithTitle message = new MessageWithTitle(R.string.fatal_error, R.string.init_database_failed);
        IllegalStateException exception = new IllegalStateException("Illegal State Exception");
        viewModel.handleFatalError(message, exception);
        viewModel.onFatalErrorMessageDismissed();
    } */

    @Test
    public void whenOnFatalErrorMessageDismissedCalledWhenCurrentViewStateIsNotShowFatalErrorMessageState_throwsException() {
        thrown.expect(ClassCastException.class);

        LiveData<MainActivityViewState> liveData = viewModel.getMainActivityViewStateObservable();

        viewModel.onFatalErrorMessageDismissed();
    }

    @Test
    public void whenOnStorageInitializedIsCalled_resultsInRequestRuntimePermissionState() {
        viewModel.onStorageInitialized();

        assert(viewModel.getMainActivityViewStateObservable().getValue() instanceof RequestRuntimePermissionState);
        RequestRuntimePermissionState state = (RequestRuntimePermissionState) viewModel.getMainActivityViewStateObservable().getValue();
        assertNotNull(state.request);
        assert(state.request.getPermission().equals(android.Manifest.permission.ACCESS_FINE_LOCATION));
        assertNotNull(state.request.getPermissionRationaleMessage());
    }

    @Test
    public void whenOnPermissionDeniedIsCalled_resultsInShowFatalErrorMessageState() {
        viewModel.onPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION);

        assert(viewModel.getMainActivityViewStateObservable().getValue() instanceof ShowFatalErrorMessageState);
        ShowFatalErrorMessageState state = (ShowFatalErrorMessageState) viewModel.getMainActivityViewStateObservable().getValue();
        assert(state.message.titleResId == R.string.fatal_error);
        assert(state.message.messageResId == R.string.location_permission_is_required);
    }

    @Test
    public void whenOnPermissionGrantedIsCalled_resultsInCheckPlayServicesAvailabilityState() {
        viewModel.onPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION);

        assert(viewModel.getMainActivityViewStateObservable().getValue() instanceof MainActivityViewState.CheckPlayServicesAvailabilityState);
    }
}