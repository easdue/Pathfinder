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
import nl.erikduisters.pathfinder.data.usecase.InitDatabase;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.FinishState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.RequestRuntimePermissionState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowFatalErrorMessageState;
import nl.erikduisters.pathfinder.ui.activity.main_activity.MainActivityViewState.ShowMessageState;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

/**
 * Created by Erik Duisters on 08-06-2018.
 */
@RunWith(JUnit4.class)
public class MainActivityViewModelTest {
    @Mock
    private Observer<MainActivityViewState> observer;
    @Mock
    private InitDatabaseHelper initDatabaseHelper;

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void whenCreated_startsWithInitDatabaseState() {
        MainActivityViewModel viewModel = new MainActivityViewModel(initDatabaseHelper);
        LiveData<MainActivityViewState> liveData = viewModel.getViewStateObservable();
        liveData.observeForever(observer);

        verify(observer).onChanged(ArgumentMatchers.isA(MainActivityViewState.InitDatabaseState.class));
    }

    @Test
    public void whenOnDatabaseInitializationProgressCalled_resultsInInitDatabaseState() {
        MainActivityViewModel viewModel = new MainActivityViewModel(initDatabaseHelper);
        LiveData<MainActivityViewState> liveData = viewModel.getViewStateObservable();

        InitDatabase.Progress progress = new InitDatabase.Progress(25, R.string.migration_1_to_2);
        viewModel.onDatabaseInitializationProgress(progress);

        assert(liveData.getValue() instanceof MainActivityViewState.InitDatabaseState);
        MainActivityViewState.InitDatabaseState state = (MainActivityViewState.InitDatabaseState) liveData.getValue();
        assertEquals(progress, state.progress);
    }

    @Test
    public void whenOnDatabaseInitializationCompleteCalled_resultsInInitStorageViewState() {
        MainActivityViewModel viewModel = new MainActivityViewModel(initDatabaseHelper);
        LiveData<MainActivityViewState> liveData = viewModel.getViewStateObservable();

        viewModel.onDatabaseInitializationComplete();

        assert(liveData.getValue() instanceof MainActivityViewState.InitStorageViewState);
    }

    @Test
    public void whenOnDatabaseInitializationErrorCalled_resultsInShowFatalErrorMessageState() {
        MainActivityViewModel viewModel = new MainActivityViewModel(initDatabaseHelper);
        LiveData<MainActivityViewState> liveData = viewModel.getViewStateObservable();

        Throwable throwable = new IllegalStateException("This cannot be");
        viewModel.onDatabaseInitializationError(throwable);

        assert(liveData.getValue() instanceof ShowFatalErrorMessageState);
        ShowFatalErrorMessageState state = (ShowFatalErrorMessageState) liveData.getValue();
        assertEquals(throwable, state.throwable);
    }

    @Test
    public void whenOnFatalErrorMessageDismissedCalledWithoutThrowable_resultsInFinishedState() {
        MainActivityViewModel viewModel = new MainActivityViewModel(initDatabaseHelper);
        LiveData<MainActivityViewState> liveData = viewModel.getViewStateObservable();

        MessageWithTitle message = new MessageWithTitle(R.string.fatal_error, R.string.init_database_failed);
        viewModel.handleFatalError(message, null);
        viewModel.onFatalErrorMessageDismissed();

        assert(liveData.getValue() instanceof FinishState);
    }

    @Test
    public void whenOnFatalErrorMessageDismissedCalledWithThrowable_throwsException() {
        thrown.expect(RuntimeException.class);

        MainActivityViewModel viewModel = new MainActivityViewModel(initDatabaseHelper);
        LiveData<MainActivityViewState> liveData = viewModel.getViewStateObservable();

        MessageWithTitle message = new MessageWithTitle(R.string.fatal_error, R.string.init_database_failed);
        IllegalStateException exception = new IllegalStateException("Illegal State Exception");
        viewModel.handleFatalError(message, exception);
        viewModel.onFatalErrorMessageDismissed();
    }

    @Test
    public void whenOnFatalErrorMessageDismissedCalledWhenCurrentViewStateIsNotShowFatalErrorMessageState_throwsException() {
        thrown.expect(ClassCastException.class);
        MainActivityViewModel viewModel = new MainActivityViewModel(initDatabaseHelper);
        LiveData<MainActivityViewState> liveData = viewModel.getViewStateObservable();

        viewModel.onFatalErrorMessageDismissed();
    }

    @Test
    public void whenHandleMessageCalled_resultsInShowMessageViewState() {
        MainActivityViewModel viewModel = new MainActivityViewModel(initDatabaseHelper);
        LiveData<MainActivityViewState> liveData = viewModel.getViewStateObservable();

        MainActivityViewState prevState = liveData.getValue();

        MessageWithTitle messageWithTitle = new MessageWithTitle(R.string.storage_error, R.string.storage_adopted_storage_unsupported);

        viewModel.handleMessage(messageWithTitle);

        MainActivityViewState newState = liveData.getValue();

        assert(newState instanceof ShowMessageState);

        ShowMessageState showMessageState = (ShowMessageState) newState;

        assert(showMessageState.message == messageWithTitle);
        assert(showMessageState.prevState == prevState);
    }

    @Test
    public void whenOnMessageDismissedIsCalled_resultsInPreviousViewState() {
        MainActivityViewModel viewModel = new MainActivityViewModel(initDatabaseHelper);
        viewModel.onStorageInitialized();

        MainActivityViewState prevState = viewModel.getViewStateObservable().getValue();

        viewModel.handleMessage(new MessageWithTitle(1,2));

        viewModel.onMessageDismissed();

        assert(viewModel.getViewStateObservable().getValue() == prevState);
    }

    @Test
    public void whenOnMessageDismissedIsCalledWhenCurrentViewStateIsNotShowMessageViewState_throwsException() {
        thrown.expect(ClassCastException.class);

        MainActivityViewModel viewModel = new MainActivityViewModel(initDatabaseHelper);
        viewModel.onStorageInitialized();

        viewModel.onMessageDismissed();
    }

    @Test
    public void whenOnStorageInitializedIsCalled_resultsInRequestRuntimePermissionState() {
        MainActivityViewModel viewModel = new MainActivityViewModel(initDatabaseHelper);
        viewModel.onStorageInitialized();

        assert(viewModel.getViewStateObservable().getValue() instanceof RequestRuntimePermissionState);
        RequestRuntimePermissionState state = (RequestRuntimePermissionState) viewModel.getViewStateObservable().getValue();
        assertNotNull(state.request);
        assert(state.request.getPermission().equals(android.Manifest.permission.ACCESS_FINE_LOCATION));
        assertNotNull(state.request.getPermissionRationaleMessage());
    }

    @Test
    public void whenOnPermissionDeniedIsCalled_resultsInShowFatalErrorMessageState() {
        MainActivityViewModel viewModel = new MainActivityViewModel(initDatabaseHelper);
        viewModel.onPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION);

        assert(viewModel.getViewStateObservable().getValue() instanceof ShowFatalErrorMessageState);
        ShowFatalErrorMessageState state = (ShowFatalErrorMessageState) viewModel.getViewStateObservable().getValue();
        assert(state.message.titleResId == R.string.fatal_error);
        assert(state.message.messageResId == R.string.location_permission_is_required);
    }

    @Test
    public void whenOnPermissionGrantedIsCalled_resultsInCheckPlayServicesAvailabilityState() {
        MainActivityViewModel viewModel = new MainActivityViewModel(initDatabaseHelper);
        viewModel.onPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION);

        assert(viewModel.getViewStateObservable().getValue() instanceof MainActivityViewState.CheckPlayServicesAvailabilityState);
    }
}