# PersonAI Technical Guide

## Introduction

This guide is intended for those with a technical background who are interested in the architecture and setup of PersonAI. The project aims to democratize access to AI by serving as a bridge between the user and a Language Learning Model (LLM), specifically ChatGPT 3.5 Turbo 16K via the OpenAI API.

## Architecture Overview

The system consists of three main components:

1. **OpenAI Account**: The user needs a standard OpenAI account with a subscription costing $20/month.
2. **Vagrant Box**: This can be set up either locally on a capable PC/laptop or on a cloud service like Linode or Digital Ocean. Docker containers are also an option.
3. **Local Gateway Tool**: This tool, part of the PersonAI project, acts as the intermediary between the Vagrant box and the LLM.

### Workflow

1. The user interacts with the Local Gateway Tool.
2. The tool forwards the user's queries to the LLM via the OpenAI API.
3. The LLM performs tasks, keeps notes, and can even install software or interact with web services on the Vagrant box.
4. Responses and results are sent back to the user through the Local Gateway Tool.

## Setup Instructions

### OpenAI Account

1. Sign up for an OpenAI account and subscribe to the $20/month plan.

### Vagrant Box Setup

1. Install Vagrant and VirtualBox.
2. Initialize a new Vagrant box.
3. SSH into the Vagrant box to configure it.

*Note: Detailed instructions for setting up a Vagrant box or Docker container will be provided.*

### Local Gateway Tool

1. Clone the PersonAI repository.
2. Navigate to the directory and run the tool.

*Note: Detailed instructions for setting up and running the Local Gateway Tool will be provided.*

## Conclusion

This guide provides a high-level overview of the PersonAI architecture and components. Detailed setup instructions will follow in subsequent sections. The aim is to make powerful AI capabilities accessible to everyone, regardless of their technical expertise.
