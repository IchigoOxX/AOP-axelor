import React, { useEffect, useRef, useState } from "react";
import styled from "styled-components";
import {useDispatch, useSelector} from "react-redux";
import MoreHorizIcon from "@mui/icons-material/MoreHoriz";
import { Menu, MenuItem } from "@mui/material";
import { useNavigate } from "react-router-dom";
import NotesOutlinedIcon from "@mui/icons-material/NotesOutlined";
import CurrencyExchangeOutlinedIcon from "@mui/icons-material/CurrencyExchangeOutlined";
import LocalPrintshopOutlinedIcon from "@mui/icons-material/LocalPrintshopOutlined";
import PrintReceipt from "./PrintReceipt";
import {deleteTicketThunk, fetchSaleOrdersThunk} from "../Redux/ticketSlice";
import DeleteOutlineOutlinedIcon from "@mui/icons-material/DeleteOutlineOutlined";
import {fetchShiftsThunk} from "../Redux/shiftSlice";

const Container = styled.div`
  margin-bottom: 20px;
`;

const OrdersTable = styled.table`
  margin: 0 20px;
  background-color: #f1f7fb;
  border-radius: 10px;
  width: calc(100% - 40px);
  overflow: auto;
  text-align: left;
  border-collapse: collapse;
  & > th,
  tr {
    padding: 10px;
  }
`;

const TableRow = styled.tr`
  border-top: solid lightgrey 1px;
  &:hover {
    background-color: #e0f3ff;
    cursor: pointer;
  }
`;

const Th = styled.th`
  padding: 7px;
`;

const Td = styled.td`
  padding: 5px;
`;

const Input = styled.input`
  border: none;
  outline-style: none;
  background-color: transparent;
`;

const MoreIcon = styled(MoreHorizIcon)`
  border-radius: 5px;
  cursor: pointer;
  &:hover {
    background-color: lightgrey;
  }
`;

const OrdersList = ({ selectedTickets, setSelectedTickets }) => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const [printPopup, setPrintPopup] = useState(false);
  const tickets = useSelector((state) => state.tickets.tickets);
  const loading = useSelector(state => state.tickets.loading);
  const error = useSelector(state => state.tickets.error);
  const [menuState, setMenuState] = useState({
    anchorEl: null,
    ticketId: null,
  });
  const [searchFields, setSearchFields] = useState({
    saleOrderSeq: "",
    clientName: "",
    startDate: "",
    endDate: "",
    statusSelect: "",
  });
  useEffect(() => {
    dispatch(fetchSaleOrdersThunk());
  }, [dispatch]);
  const getStatusLabel = (status) => {
    switch (status) {
      case 1:
        return 'Draft';
      case 2:
        return 'Finalized';
      case 3:
        return 'Confirmed';
      case 4:
        return 'Completed';
      case 5:
        return 'cancelled';
      default:
        return '_';
    }
  };
  const handleSelectTicket = (ticket) => {
    setSelectedTickets((prevSelected) => {
      const newSelected = prevSelected.includes(ticket)
        ? prevSelected.filter((oldTicket) => oldTicket !== ticket)
        : [...prevSelected, ticket];
      return newSelected;
    });
  };

  const handleSelectAll = () => {
    if (selectedTickets.length === tickets.length) {
      setSelectedTickets([]);
    } else {
      setSelectedTickets(tickets.map((ticket) => ticket));
    }
  };

  const isAllSelected = tickets.length > 0 && selectedTickets.length === tickets.length;

  const handleClick = (event, ticketId) => {
    setMenuState({ anchorEl: event.currentTarget, ticketId });
  };

  const handleClose = () => {
    setMenuState({ anchorEl: null, ticketId: null });
  };

  const handleDetails = (ticket) => {
    handleClose();
    navigate("/orders/order-details", {
      state: { saleOrderSeq: ticket.saleOrderSeq, mode: "view" },
    });
  };

  const handleRefund = (ticket) => {
    handleClose();
    navigate("/orders/order-details", {
      state: { saleOrderSeq: ticket.saleOrderSeq, mode: "refund" },
    });
  };

  const handleSearchChange = (field) => (event) => {
    setSearchFields({ ...searchFields, [field]: event.target.value });
  };

  const filteredTickets = tickets.filter((ticket) => {
    const { saleOrderSeq, clientName, startDate, endDate, statusSelect } = searchFields;
    const ticketNum = ticket.saleOrderSeq?.toLowerCase() || "";
    const clientPartnerName = ticket.clientPartner?.simpleFullName?.toLowerCase() || "";
    const creationDate = ticket.creationDate || null;
    // const creationDate = ticket.creationDate ? ticket.creationDate : null;

    const start = startDate ? new Date(startDate) : null;
    const end = endDate ? new Date(endDate) : null;

    return (
      ticketNum.includes(saleOrderSeq.toString().toLowerCase()) &&
      clientPartnerName.includes(clientName?.toLowerCase()) &&
      (!start || (creationDate && creationDate >= start)) &&
      (!end || (creationDate && creationDate <= end)) &&
      (!statusSelect || ticket.statusSelect.toString() === statusSelect)
    );
  });

  const handlePrint = () => {
    setMenuState({ anchorEl: null, ticketId: null });
    setPrintPopup(true);
  };

  const handleAfterPrint = () => {
    setPrintPopup(false);
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <Container>
      <OrdersTable>
        <thead>
          <tr>
            <Th>
              <input
                type="checkbox"
                checked={isAllSelected}
                onChange={handleSelectAll}
              />
            </Th>
            <Th>Ticket Number</Th>
            <Th>Status</Th>
            <Th>Customer</Th>
            <Th>Creation Date</Th>
            <Th>Creation Time</Th>
            <Th></Th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <Td></Td>
            <Td>
              <Input
                type="text"
                value={searchFields.saleOrderSeq}
                onChange={handleSearchChange("saleOrderSeq")}
                placeholder="Search Ticket Number"
              />
            </Td>
            <Td>
              <select
                  value={searchFields.statusSelect}
                  onChange={handleSearchChange("statusSelect")}
              >
                <option value="">All</option>
                <option value="1">Draft</option>
                <option value="3">Confirmed</option>
                <option value="4">Completed</option>
                <option value="5">Cancelled</option>
              </select>
            </Td>
            <Td>
              <Input
                type="text"
                value={searchFields.clientName}
                onChange={handleSearchChange("clientName")}
                placeholder="Search Customer"
              />
            </Td>
            <Td>
              <Input
                type="date"
                value={searchFields.startDate}
                onChange={handleSearchChange("startDate")}
                placeholder="Start Date"
              />
              <Input
                type="date"
                value={searchFields.endDate}
                onChange={handleSearchChange("endDate")}
                placeholder="End Date"
              />
            </Td>
            <Td></Td>
            <Td></Td>
          </tr>
          {filteredTickets.map((ticket) => (
            <>
              <TableRow key={ticket.saleOrderSeq} onDoubleClick={() => handleDetails(ticket)}>
                <Td>
                  <input
                    type="checkbox"
                    checked={selectedTickets.includes(ticket)}
                    onChange={() => handleSelectTicket(ticket)}
                  />
                </Td>
                <Td>{ticket.saleOrderSeq}</Td>
                <Td>{getStatusLabel(ticket.statusSelect)}</Td>
                <Td>{ticket.clientPartner?.simpleFullName || "_"}</Td>
                <Td>{ticket.creationDate || "_"}</Td>
                <Td>{ticket.creationDate || "_"}</Td>
                <Td>
                  <MoreIcon
                    aria-controls="simple-menu"
                    aria-haspopup="true"
                    onClick={(event) => handleClick(event, ticket.saleOrderSeq)}
                  />
                </Td>
                <Menu
                  id="simple-menu"
                  anchorEl={menuState.anchorEl}
                  keepMounted
                  open={menuState.ticketId === ticket.saleOrderSeq}
                  onClose={handleClose}
                >
                  <MenuItem onClick={() => handleDetails(ticket)}>
                    <NotesOutlinedIcon />
                    View Details
                  </MenuItem>
                  <MenuItem onClick={() => handleRefund(ticket)}>
                    <CurrencyExchangeOutlinedIcon />
                    Refund
                  </MenuItem>
                  <MenuItem onClick={handlePrint}>
                    <LocalPrintshopOutlinedIcon />
                    Print
                  </MenuItem>
                  { ticket.statusSelect === 1 && (
                      <MenuItem onClick={() => dispatch(deleteTicketThunk(ticket.id))}>
                        <DeleteOutlineOutlinedIcon />
                        Delete
                      </MenuItem>
                  )}
                </Menu>
              </TableRow>
              {printPopup && (
                <PrintReceipt
                  tickets={[ticket]}
                  handlePrint={handleAfterPrint}
                  handlePrintCancel={handleAfterPrint}
                />
              )}
            </>
          ))}
        </tbody>
      </OrdersTable>
    </Container>
  );
};

export default OrdersList;
