/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import React from 'react';
import { HashRouter as Router, Route, Routes } from 'react-router-dom';
import HomePage from './Components/Pages/HomePage';
import OrdersPage from './Components/Pages/OrdersPage';
import ShiftsPage from './Components/Pages/ShiftsPage';
import CustomersPage from './Components/Pages/CustomersPage';
import ItemsPage from './Components/Pages/ItemsPage';
import SettingsPage from './Components/Pages/SettingsPage';
import PaymentPage from './Components/Pages/PaymentPage';
import CustomerDetailsPage from './Components/Pages/CustomerDetailsPage';
import OrderDetailsPage from './Components/Pages/OrderDetailsPage';
import ProductDetailsPage from './Components/Pages/ProductDetailsPage';
import ShiftDetailsPage from "./Components/Pages/ShiftDetailsPage";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/payment" element={<PaymentPage />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/orders/order-details" element={<OrderDetailsPage />} />
        <Route path="/shifts" element={<ShiftsPage />} />
        <Route path="/shifts/shift-details" element={<ShiftDetailsPage />} />
        <Route path="/items" element={<ItemsPage />} />
        <Route path="/items/item-details" element={<ProductDetailsPage />} />
        <Route path="/customers" element={<CustomersPage />} />
        <Route path="/customers/customer-details" element={<CustomerDetailsPage />} />
        <Route path="/settings" element={<SettingsPage />} />
      </Routes>
    </Router>
    /*<HomePage />*/
  );
}

export default App;
