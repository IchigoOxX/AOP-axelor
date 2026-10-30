import React, { useEffect, useState } from "react";
import {
  FormControl,
  FormHelperText,
  Input,
  NativeSelect,
} from "@mui/material";
import MuiPhoneNumber from "mui-phone-number";
import InfoOutlined from "@mui/icons-material/InfoOutlined";
import styled from "styled-components";

const Div = styled(FormControl)`
  display: flex;
`;
const Wrapper = styled.div`
  display: grid;
  grid-template-columns: 1fr 3fr;
  align-items: center;
`;
const Label = styled.div`
  margin: auto auto auto 0px;
`;
const InputWrapper = styled.div`
  display: grid;
`;

const InputWithValidation = (props) => {
  const [touched, setTouched] = useState(false);
  const [emailError, setEmailError] = useState(null);
  const generalError = props.validator ? !props.validator(props.value) : null;

  const handleBlur = () => {
    if (props.value) {
      setTouched(true);
      if (props.type === "email") {
        setEmailError(!props.validator(props.value));
      }
    }
  };

  const handleChange = (event) => {
    const { value } = event.target;
    props.handleChange(event);
    if (props.type === "email") {
      if (!value) {
        setEmailError(false);
        setTouched(false);
      } else if (touched) {
        setEmailError(!props.validator(value));
      }
    }
  };

  // Function to extract country code and phone number
  const extractCountryCodeAndNumber = (phone) => {
    const countryCodeMatch = phone.match(/^\+(\d{1,4})\s/);
    const countryCode = countryCodeMatch ? `+${countryCodeMatch[1]}` : "";
    // const phoneNumber = phone.replace(countryCode, "").trim();
    const phoneNumber = phone.trim();
    console.log('countryCode1: ', countryCode);
    console.log('phoneNumber1: ', phoneNumber)
    return { countryCode, phoneNumber };
  };
 console.log('type: ', props.type);
  const { countryCode, phoneNumber } = props.type === "phone" && props.value
      ? extractCountryCodeAndNumber(props.value)
      : { countryCode: "", phoneNumber: props.value };
 console.log('countryCode: ', countryCode);
 console.log('phoneNumber: ', phoneNumber)
  return (
      <Div error={props.type === "email" ? emailError : generalError}>
        <Wrapper>
          <Label>{props.label}</Label>
          <InputWrapper>
            {props.type === "phone" ? (
                <MuiPhoneNumber
                    defaultCountry={countryCode ? countryCode : props.defaultCountry || "fr"}
                    label={props.label}
                    name={props.name}
                    value={phoneNumber}
                    onChange={(value) =>
                        props.handleChange({ target: { name: props.name, value: `${countryCode} ${value}` } })
                    }
                    disabled={props.disabled}
                />
            ) : props.options ? (
                <NativeSelect
                    defaultValue=""
                    value={props.value}
                    onChange={props.handleChange}
                    inputProps={{
                      name: props.name,
                      id: props.name,
                    }}
                    placeholder={props.placeholder}
                    disabled={props.disabled}
                >
                  <option value="" disabled>
                    None Selected
                  </option>
                  {props.options.map((option) => (
                      <option key={option} value={option.id}>
                        {option.name ? option.name : option}
                      </option>
                  ))}
                </NativeSelect>
            ) : (
                <Input
                    type={props.type}
                    name={props.name}
                    placeholder={props.placeholder}
                    value={props.value}
                    onChange={handleChange}
                    onBlur={props.type === "email" ? handleBlur : undefined}
                    disabled={props.disabled}
                />
            )}
            {(props.type === "email" ? emailError : generalError) && (
                <FormHelperText>
                  <InfoOutlined fontSize="sm" />
                  &nbsp;{props.errorMessage}
                </FormHelperText>
            )}
          </InputWrapper>
        </Wrapper>
      </Div>
  );
};

export default InputWithValidation;
