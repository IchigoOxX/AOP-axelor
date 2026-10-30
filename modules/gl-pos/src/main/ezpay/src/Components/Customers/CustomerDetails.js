import React, { useEffect, useState } from 'react';
import styled from 'styled-components';
import FeedOutlinedIcon from '@mui/icons-material/FeedOutlined';
import LocalPhoneOutlinedIcon from '@mui/icons-material/LocalPhoneOutlined';
import LocationOnOutlinedIcon from '@mui/icons-material/LocationOnOutlined';
import CardMembershipOutlinedIcon from '@mui/icons-material/CardMembershipOutlined';
import AddOutlinedIcon from '@mui/icons-material/AddOutlined';
import { useNavigate } from 'react-router-dom';
import InputWithValidation from '../Common/InputWithValidation';
import { selectCustomer, addCustomerThunk, updateCustomerThunk } from '../Redux/customerSlice';
import { useDispatch } from 'react-redux';
import countriesData from '../../data/countries.json';
import { validateEmail, validateName, validateNumber } from '../../utils';
import SectionComponent from './SectionComponent';
import { IconButton, TextField } from '@mui/material';
import QrCodeScannerOutlinedIcon from '@mui/icons-material/QrCodeScannerOutlined';
import Scanning from '../Common/Scanning';
import CheckOutlinedIcon from '@mui/icons-material/CheckOutlined';
import LoyaltyCard from './LoyaltyCard';
import CachedOutlinedIcon from '@mui/icons-material/CachedOutlined';
import {fetchCities, fetchCountries} from "../../Services/generalApi";
import loyaltyCard from "./LoyaltyCard";
import CloseOutlinedIcon from "@mui/icons-material/CloseOutlined";


const Container = styled.div`
  margin: 20px 100px;
  display: flex;
  flex-direction: column;
  gap: 10px;
`;
const Button = styled.button`
  display: flex;
  align-items: center;
  margin: auto;
  padding: 10px;
  gap: 5px;
  background-color: #a8d0e7;
  color: #0c085c;
  font-size: 18px;
  font-weight: 600;
  border: none;
  border-radius: 10px;
  box-shadow: 0px 2px 4px 1px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  transition: box-shadow 0.3s ease;
  &:hover {
    box-shadow: 0px 2px 10px 1px rgba(0, 0, 0, 0.4);
  }
`;
const CardRef = styled.div`
  display: flex;
  align-items: center;
  margin: auto;
  margin-bottom: 20px;
`;
const ScanIcon = styled(QrCodeScannerOutlinedIcon)`
  padding: 20px 0 0 20px;
`;
const CancelIcon = styled(CloseOutlinedIcon)`
  padding: 20px 0 0 10px;
  transition: color 0.3s ease;
  &:hover {
    color: red;
  }
`;
const LoyaltyCardDiv = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
`;
const RegenerateButton = styled.button`
  position: absolute;
  top: 10px;
  right: 10px;
  margin: 10px;
  display: flex;
  align-items: center;
  padding: 5px 10px;
  background-color: white;
  color: #00796f;
  border: solid #a8d0e7 1px;
  border-radius: 10px;
  box-shadow: 0 2px 3px 0 rgba(168, 208, 231, 0.7);
  cursor: pointer;
  transition:
    background-color 0.3s,
    box-shadow 0.3s,
    transform 0.3s;

  &:hover {
    background-color: #f0f0f0;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    transform: translateY(-2px);
  }

  &:active {
    background-color: #e0e0e0;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    transform: translateY(0);
  }

  & > svg {
    margin-right: 5px;
  }
`;

const RefreshIcon = styled(CachedOutlinedIcon)`
  font-size: 18px;
  color: inherit;
`;

const CustomerDetails = ({ customer, mode, returnToHomePage }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    simpleFullName: customer?.simpleFullName || null,
    firstName: customer?.firstName || null,
    name: customer?.name || null,
    nic: customer?.nic || null,
    dateOfBirth: customer?.dateOfBirth || null,
    placeOfBirth: customer?.placeOfBirth || null,
    mobilePhone: customer?.mobilePhone? customer.mobilePhone : customer?.fixedPhone? customer.fixedPhone : null,
    emailAddress: customer?.emailAddress || {},
    country: customer?.mainAddress?.addressL7Country || {},
    city: customer?.mainAddress?.city || {},
    street: customer?.mainAddress?.addressL4 || null,
    loyaltyCard: customer?.loyaltyCard || {
      cardNumber: null
    }
  });

  const [countries, setCountries] = useState([]);
  const [cities, setCities] = useState([]);
  const [defaultCountry, setDefaultCountry] = useState('');

  useEffect(() => {
    const fetchCountriesData = async () => {
      try {
        const countriesData = await fetchCountries(); // Assuming fetchCountries returns an array of countries
        setCountries(countriesData);
      } catch (error) {
        console.error('Error fetching countries:', error);
      }
    };
    fetchCountriesData();
  }, []);

  useEffect(() => {
    if(formData.country) {
      const fetchCitiesData = async () => {
        try {
          const citiesData = await fetchCities(formData.country);
          setCities(citiesData);
        } catch (error) {
          console.error('Error fetching cities:', error);
        }
      };
      fetchCitiesData()
    }
  }, [formData.country]);

  useEffect(() => {
    const detectUserCountry = () => {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          async position => {
            const { latitude, longitude } = position.coords;
            try {
              const response = await fetch(
                `https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json`,
              );
              const data = await response.json();
              if (data && data.address && data.address.country_code) {
                setDefaultCountry(data.address.country_code.toLowerCase());
                // setFormData(prevFormData => ({
                //   ...prevFormData,
                //   country: data.address.country,
                // }));
              }
            } catch (error) {
              console.error('Error fetching location:', error);
            }
          },
          error => {
            console.error('Error getting geolocation:', error);
          },
        );
      }
    };
    detectUserCountry();
  }, []);
  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name.startsWith("loyaltyCard")) {
      // Handle nested updates for loyalty card fields
      setFormData(prevFormData => ({
        ...prevFormData,
        loyaltyCard: {
          ...prevFormData.loyaltyCard,
          [name.split('.')[1]]: value // Extract the nested field and update it
        }
      }));
    } else if (name === "country") {
      const selectedCountry = countries.find((country) => country.id === Number(value));
      setFormData((prevFormData) => ({
        ...prevFormData,
        country: selectedCountry,
        city: ''
      }));
    } else if (name === "city") {
      const selectedCity = cities.find((city) => city.id === Number(value));
      setFormData((prevFormData) => ({
        ...prevFormData,
        city: selectedCity
      }));
    } else {
      setFormData({ ...formData, [name]: value });
    }
  };
  const addCustomer = event => {
    const newCustomer = {
      simpleFullName: formData.name ? `${formData.name} ${formData.firstName}` : formData.firstName,
      firstName: formData.firstName,
      name: formData.name,
      nic: formData.nic,
      dateOfBirth: formData.dateOfBirth,
      placeOfBirth: formData.placeOfBirth,
      mobilePhone: formData.mobilePhone,
      emailAddress: formData.emailAddress,
      mainAddress: {
        addressL7Country: formData.country,
        city: formData.city,
        addressL4: formData.street,
      },
      loyaltyCard: formData.loyaltyCard.cardNumber? formData.loyaltyCard : null,
    };
    dispatch(addCustomerThunk(newCustomer))
      .then(() => {
        if (returnToHomePage) {
          dispatch(selectCustomer({ customer: newCustomer }));
          returnToHomePage(event);
        } else {
          navigate("/customers");
        }
      })
      .catch(error => {
        console.error('Error adding customer:', error);
      });
  };

  const [showLoyaltyCardRefInput, setShowLoyaltyCardRefInput] = useState(false);
  const handleClickAddLoyaltyCard = () => {
    setShowLoyaltyCardRefInput(true);
  };
  const cancel = () => {
    setShowLoyaltyCardRefInput(false)
    setFormData(prevFormData => ({
      ...prevFormData,
      loyaltyCard: {
        cardNumber: null
      }
    }))
  }
  const addLoyaltyCard = () => {
    setFormData(prevFormData => ({
      ...prevFormData,
      loyaltyCard: {
        cardNumber: formData.loyaltyCard.cardNumber,
        points: customer?.loyaltyCard?.points || 0,
        // creationDate: new Date().toISOString().split('T')[0],
        creationDate: new Date().toLocaleDateString('fr-FR'),
        expirationDate: new Date(new Date().setFullYear(new Date().getFullYear() + 4)).toISOString().split('T')[0]
      }
    }));
    setShowLoyaltyCardRefInput(false);
  };
  const [isScanOpen, setIsScanOpen] = useState(false);
  const openScan = () => {
    setIsScanOpen(true);
  };
  const closeScan = () => {
    setIsScanOpen(false);
  };

  const updateCustomer = event => {
    const updatedCustomer = {
      ...customer,
      simpleFullName: formData.name ? `${formData.name} ${formData.firstName}` : formData.firstName,
      firstName: formData.firstName,
      name: formData.name,
      nic: formData.nic,
      dateOfBirth: formData.dateOfBirth,
      placeOfBirth: formData.placeOfBirth,
      mobilePhone: formData.mobilePhone,
    }
    if (formData.emailAddress) {
      console.log("update email: ", formData.emailAddress)
      updatedCustomer.emailAddress = {
        ...customer.emailAddress,
        address: formData.emailAddress,
      }
    }
    if (Object.keys(formData.country).length !== 0) {
      console.log("update mainAddress: ", formData.country)
      updatedCustomer.mainAddress = {
        ...customer.mainAddress,
        addressL7Country: formData.country,
        city: formData.city,
        addressL4: formData.street
      }
    }
    if (formData.loyaltyCard.cardNumber) {
      console.log("update loyaltyCard: ", formData.loyaltyCard.cardNumber)
      updatedCustomer.loyaltyCard = {
        ...customer.loyaltyCard,
        cardNumber: formData.loyaltyCard.cardNumber,
        points: formData.loyaltyCard.points,
        creationDate: formData.loyaltyCard.creationDate,
        expirationDate: formData.loyaltyCard.expirationDate
      };
    }
    console.log("update customer data: ", updatedCustomer)
    dispatch(updateCustomerThunk(updatedCustomer));
    if (returnToHomePage) {
      dispatch(selectCustomer(updatedCustomer));
      returnToHomePage(event);
    } else {
      navigate("/customers");
    }
  };
console.log('country1: ', customer?.mainAddress?.addressL7Country)
  console.log("formData1: ", formData);
  useEffect(() => {
    console.log("formData: ", formData);
    console.log("show input card ref: ", showLoyaltyCardRefInput);
  }, [formData, showLoyaltyCardRefInput]);
  return (
    <Container>
      <SectionComponent title="Personal Details" icon={<FeedOutlinedIcon />}>
        {mode === 'view' ? (
          <>
            <div>
              <strong>First Name:</strong> {customer.firstName ? customer.firstName : ''}
            </div>
            <div>
              <strong>Last Name:</strong> {customer.name ? customer.name : ''}
            </div>
            <div>
              <strong>NIC:</strong> {customer.nic ? customer.nic : ''}
            </div>
            <div>
              <strong>Date of Birth:</strong> {customer.dateOfBirth ? customer.dateOfBirth : ''}
            </div>
            <div>
              <strong>Place of Birth:</strong> {customer.placeOfBirth ? customer.placeOfBirth : ''}
            </div>
          </>
        ) : (
          <>
            <InputWithValidation
              label="First Name:"
              type="text"
              name="firstName"
              value={formData.firstName}
              handleChange={handleChange}
              placeholder="Enter first name..."
              errorMessage="First name should only contain letters."
              validator={validateName}
            />
            <InputWithValidation
              label="Last Name:"
              type="text"
              name="name"
              value={formData.name}
              handleChange={handleChange}
              placeholder="Enter last name..."
              errorMessage="Last name should only contain letters."
              validator={validateName}
            />
            <InputWithValidation
              label="NIC:"
              type="text"
              name="nic"
              value={formData.nic}
              handleChange={handleChange}
              placeholder="Enter NIC..."
              errorMessage="NIC should only contain numbers."
              validator={validateNumber}
            />
            <InputWithValidation
              label="Date of Birth:"
              type="date"
              name="dateOfBirth"
              value={formData.dateOfBirth}
              handleChange={handleChange}
            />
            <InputWithValidation
              label="Place of Birth:"
              type="select"
              name="placeOfBirth"
              value={formData.placeOfBirth}
              handleChange={handleChange}
              options={countries.map(country => country.name)}
            />
          </>
        )}
      </SectionComponent>
      <SectionComponent title="Contact Info" icon={<LocalPhoneOutlinedIcon />}>
        {mode === 'view' ? (
          <>
            <div>
              <strong>Phone:</strong>{' '}
              {customer.mobilePhone ? customer.mobilePhone : customer.fixedPhone ? customer.fixedPhone : ''}
            </div>
            <div>
              <strong>Email:</strong> {customer.emailAddress
                ? customer.emailAddress.address
                : ''}
            </div>
          </>
        ) : (
          <>
            <InputWithValidation
              label="Phone:"
              type="phone"
              name="mobilePhone"
              defaultCountry={defaultCountry}
              value={formData.mobilePhone}
              handleChange={handleChange}
              placeholder="Enter phone number..."
              /*errorMessage="Phone number should only contain numbers."
                    validator={validatePhoneNumber}*/
            />
            <InputWithValidation
              label="Email:"
              type="email"
              name="emailAddress"
              value={formData.emailAddress.address}
              handleChange={handleChange}
              placeholder="Enter email address..."
              errorMessage="Email address is not valid."
              validator={validateEmail}
            />
          </>
        )}
      </SectionComponent>
      <SectionComponent title="Address" icon={<LocationOnOutlinedIcon />}>
        {mode === 'view' ? (
          <>
            <div>
              <strong>Country:</strong> { customer.mainAddress?.addressL7Country ? customer.mainAddress.addressL7Country.name : '' }
            </div>
            <div>
              <strong>City:</strong> {
              customer.mainAddress && customer.mainAddress.city && customer.mainAddress.city.fullName
                  ? customer.mainAddress.city.fullName.split(' - ')[0].trim()
                  : ''
            }
            </div>
            <div>
              <strong>Street (optional):</strong> { customer.mainAddress?.addressL4 ?? '' }
            </div>
          </>
        ) : (
          <>
            <InputWithValidation
              label="Country:"
              type="select"
              name="country"
              value={formData.country.id}
              handleChange={handleChange}
              options={countries}
            />
            <InputWithValidation
              label="City:"
              type="select"
              name="city"
              value={formData.city.id}
              handleChange={handleChange}
              options={cities}
              disabled={!formData.country}
            />
            <InputWithValidation
              label="Street:"
              type="text"
              name="street"
              value={formData.street}
              handleChange={handleChange}
              placeholder="Enter street name and number..."
              disabled={!formData.country}
            />
          </>
        )}
      </SectionComponent>
      <SectionComponent title="Loyalty Card" icon={<CardMembershipOutlinedIcon />}>
        <>
          {formData.loyaltyCard.cardNumber && !showLoyaltyCardRefInput ? (
            mode === 'view' ? (
              <LoyaltyCardDiv>
                <div>
                  <div>
                    <strong>Card Number:</strong> {formData.loyaltyCard.cardNumber}
                  </div>
                  <div>
                    <strong>Points:</strong> {formData.loyaltyCard.points}
                  </div>
                  <div>
                    <strong>Date of Creation:</strong> {formData.loyaltyCard.creationDate}
                  </div>
                  <div>
                    <strong>Expiration Date:</strong> {formData.loyaltyCard.expirationDate}
                  </div>
                </div>
                <LoyaltyCard name={customer.simpleFullName} loyaltyCard={customer.loyaltyCard} />
              </LoyaltyCardDiv>
            ) : (
              <>
                <div>
                  <strong>Card Number:</strong> {formData.loyaltyCard.cardNumber}
                </div>
                <div>
                  <strong>Points:</strong> {formData.loyaltyCard.points}
                </div>
                <div>
                  <strong>Date of Creation:</strong> {formData.loyaltyCard.creationDate}
                </div>
                <InputWithValidation
                  label="Expiration Date:"
                  type="date"
                  name="loyaltyCard.expirationDate"
                  value={formData.loyaltyCard.expirationDate}
                  handleChange={handleChange}
                />
                <RegenerateButton onClick={handleClickAddLoyaltyCard}>
                  <RefreshIcon />
                  Regenerate
                </RegenerateButton>
              </>
            )
          ) : !showLoyaltyCardRefInput && !customer?.loyaltyCard ? (
            <Button onClick={handleClickAddLoyaltyCard}>
              <div>
                <AddOutlinedIcon fontSize="lg" />
              </div>
              Add Loyalty Card
            </Button>
          ) : (
            <CardRef>
              <TextField
                label="Enter Card Ref"
                variant="standard"
                name="loyaltyCard.cardNumber"
                value={formData.loyaltyCard.cardNumber}
                onChange={handleChange}
              />
              <ScanIcon onClick={openScan} />
              {isScanOpen && <Scanning handleClose={closeScan} />}
              <CancelIcon onClick={cancel} />
            </CardRef>
          )}
        </>
      </SectionComponent>
      {mode !== 'view' && (
        <Button onClick={mode === 'edit' ? updateCustomer : addCustomer}>
          {mode === 'edit' ? 'Save Changes' : 'Add Customer'}
        </Button>
      )}
    </Container>
  );
};

export default CustomerDetails;
