import React, { Component } from "react";
import "./Auth.css";
import AuthContext from "../context/auth-context";

class AuthPage extends Component {
  state = {
    isLogin: true,
  };

  static contextType = AuthContext;

  constructor(props) {
    super(props);
    this.emailEl = React.createRef();
    this.passwordEl = React.createRef();
  }

  switchModeHandler = () => {
    this.setState((prevState) => {
      return { isLogin: !prevState.isLogin };
    });
  };

  submitHandler = (event) => {
    event.preventDefault();
    const email = this.emailEl.current.value;
    const password = this.passwordEl.current.value;

    if (email.trim().length === 0 || password.trim().length === 0) {
      return;
    }

    let requestBody = {
      query: `
        query Login($email: String!, $password: String!) {
            login(loginInput: {email: $email, password: $password}) {
                userId,
                token,
                tokenExpiration
                baseResponse {
                  msg
                  code
                  }
            }
        }
        `,
      variables: {
        email: email,
        password: password,
      },
    };

    if (!this.state.isLogin) {
      requestBody = {
        query: `
                mutation CreateUser($email: String!, $password: String!) {
                    createUser(userInput: {
                        email: $email,
                        password: $password
                    }) {
                        user {
                        id
                        email
                        }
                        baseResponse {
                          msg
                          code
                        }
                    }
                }
              `,
        variables: {
          email: email,
          password: password,
        },
      };
    }

    console.log(email, password);
    // https://www.baeldung.com/spring-cors
    fetch("http://localhost:8080/graphql", {
      method: "POST",
      body: JSON.stringify(requestBody),
      headers: {
        "Content-Type": "application/json",
      },
    })
      .then((res) => {
        if (res.status !== 200 && res.status !== 201) {
          window.alert("Server Failed")
          throw new Error("Failed!");
        }
        return res.json();
      })
      .then((resData) => {
        console.log(resData);
        if (resData.data.login) {
          if (resData.data.login.baseResponse.code !== 200) {
            window.alert(resData.data.login.baseResponse.msg)
          }
          if (resData.data.login && resData.data.login.token) {
            this.context.login(
                resData.data.login.token,
                resData.data.login.userId,
                resData.data.login.tokenExpiration
            );
          }
        }
        if (resData.data.createUser) {
          if (resData.data.createUser.baseResponse.code === 200) {
            window.alert("Successfully Signed Up, Please Switch to Log In!")
          }
          else {
            window.alert(resData.data.createUser.baseResponse.msg)
          }
        }

      })
      .catch((err) => {
        console.log(err);
      });
  };

  render() {
    return (
      <form className="auth-form" onSubmit={this.submitHandler}>
        <div className="form-control">
          <label htmlFor="email">E-Mail</label>
          <input type="email" id="email" ref={this.emailEl} />
        </div>
        <div className="form-control">
          <label htmlFor="password">Password</label>
          <input type="password" id="password" ref={this.passwordEl} />
        </div>
        <div className="form-actions">
          <button type="submit">Submit</button>
          <button type="button" onClick={this.switchModeHandler}>
            Switch to {this.state.isLogin ? "Signup" : "Login"}
          </button>
        </div>
      </form>
    );
  }
}

export default AuthPage;
