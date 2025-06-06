package com.tfyre.bambu.view.dashboard;

import com.tfyre.bambu.BambuConfig;
import com.tfyre.bambu.printer.BambuPrinter;
import com.tfyre.bambu.MainLayout;
import com.tfyre.bambu.SystemRoles;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import com.tfyre.bambu.printer.BambuPrinters;
import com.tfyre.bambu.view.PushDiv;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.inject.Instance;

/**
 *
 * @author Francois Steyn - (fsteyn@tfyre.co.za)
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard")
@RolesAllowed({ SystemRoles.ROLE_ADMIN, SystemRoles.ROLE_NORMAL })
public class Dashboard extends PushDiv {

    @Inject
    BambuPrinters printers;
    @Inject
    Instance<DashboardPrinter> cardInstance;
    @Inject
    BambuConfig config;

    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        final List<Runnable> runnables = new ArrayList<>();
        addClassName("dashboard-view");

        printers.getPrinters()
                .stream().sorted(Comparator.comparing(BambuPrinter::getName))
                .map(printer -> handlePrinter(printer, runnables::add))
                .forEach(this::add);
        final UI ui = attachEvent.getUI();
        createFuture(() -> ui.access(() -> runnables.forEach(Runnable::run)), config.refreshInterval());
    }

    private Component handlePrinter(final BambuPrinter printer, final Consumer<Runnable> consumer) {
        final DashboardPrinter card = cardInstance.get();
        consumer.accept(card::update);
        return card.build(printer, true);
    }

}
