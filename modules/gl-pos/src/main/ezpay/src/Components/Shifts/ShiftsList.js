import React, { useEffect, useState } from "react";
import styled from "styled-components";
import { useDispatch, useSelector } from "react-redux";
import MoreHorizIcon from "@mui/icons-material/MoreHoriz";
import { Menu, MenuItem } from "@mui/material";
import { useNavigate } from "react-router-dom";

const Container = styled.div`
  margin: 20px;
`;

const ShiftsTable = styled.table`
  margin: auto;
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

const ShiftsList = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const shifts = useSelector((state) => state.shifts.shifts);
  const loading = useSelector((state) => state.shifts.loading);
  const error = useSelector((state) => state.shifts.error);
  const currentUser = useSelector((state) => state.users.currentUser);
  const [selectedShifts, setSelectedShifts] = useState([]);
  const [searchFields, setSearchFields] = useState({
    shiftNumber: "",
    shiftOwnerName: "",
    date: "",
    status: "",
  });
  const getStatusLabel = (status) => {
    switch (status) {
      case 0:
        return 'Open';
      case 1:
        return 'Closed';
    }
  };
  const handleShiftClick = (shift) => {
    navigate(`/shifts/shift-details`, { state: { shift } });
  };

  const handleSearchChange = (field) => (event) => {
    setSearchFields({ ...searchFields, [field]: event.target.value });
  };

  const isAdmin =
      currentUser && currentUser.fullName.toLowerCase() === "admin";

  const filteredShifts = shifts
      .filter((shift) => {
        const { shiftNumber, shiftOwnerName, startDate, endDate } = searchFields;
        return (
            (!shiftNumber ||
                shift.shiftNumber.toLowerCase().includes(shiftNumber.toLowerCase())) &&
            (!shiftOwnerName ||
                (shift.cashier &&
                    shift.cashier.fullName
                        .toLowerCase()
                        .includes(shiftOwnerName.toLowerCase()))) &&
            (!startDate || new Date(shift.openingHour) >= new Date(startDate)) &&
            (!endDate || new Date(shift.openingHour) <= new Date(endDate))
        );
      });

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
      <Container>
        <ShiftsTable>
          <thead>
          <tr>
            <Th>Shift Number</Th>
            <Th>Shift Owner</Th>
            <Th>Date</Th>
            <Th>Opening Hour</Th>
            <Th>Closing Hour</Th>
            <Th>Status</Th>
          </tr>
          <tr>
            <Th>
              <Input
                  type="text"
                  value={searchFields.shiftNumber}
                  onChange={handleSearchChange("shiftNumber")}
                  placeholder="Search by shift number"
              />
            </Th>
            <Th>
              <Input
                  type="text"
                  value={searchFields.shiftOwnerName}
                  onChange={handleSearchChange("shiftOwnerName")}
                  placeholder="Search by owner name"
              />
            </Th>
            <Th>
              <Input
                  type="date"
                  value={searchFields.date}
                  onChange={handleSearchChange("date")}
              />
            </Th>
            <Th />
            <Th />
            <Th>
                <select
                    value={searchFields.status}
                    onChange={handleSearchChange("status")}
                >
                  <option value="">All</option>
                  <option value="1">Open</option>
                  <option value="3">Closed</option>
                </select>
            </Th>
          </tr>
          </thead>
          <tbody>
          {filteredShifts.map((shift) => (
              <TableRow key={shift.shiftNumber}  onDoubleClick={() => handleShiftClick(shift)}>
                <Td>{shift.shiftNumber}</Td>
                <Td>{shift.cashier?.fullName}</Td>
                <Td>{new Date(shift.shiftDate).toLocaleDateString()}</Td>
                <Td>{new Date(shift.openingHour).toLocaleTimeString()}</Td>
                <Td>{shift.closingHour ? new Date(shift.closingHour).toLocaleTimeString() : '_'}</Td>
                <Td>{getStatusLabel(shift.status)}</Td>
              </TableRow>
          ))}
          </tbody>
        </ShiftsTable>
      </Container>
  );
};

export default ShiftsList;
