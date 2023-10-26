# dal-infrastructure-management-google-workspace
DAL adapter for monitoring Chrome OS devices through Google Workspace.<br>
Currently monitored property of aggregator device are:
- ChromeOSDevicesCount
- OrganizationalUnitsCount
- OrganizationalUnits
    - ChromeOSDevicesCount
    - Description
    - Name
    - ParentName
    - UnitID

Currently monitored property of aggregated device are:
- AnnotatedUser
- Architecture
- AutoUpdateExpiration
- BootMode
- ChromeOSVersion
- CPUModel
- CPUUtilization(%)
- deviceId
- deviceModel
- deviceName
- deviceOnline
- EnrollmentTime
- FirmwareVersion
- IPAddress
- LastSync
- MACAddress
- MemoryFree(GB)
- MemoryTotal(GB)
- OrgUnit
- PlatformVersion
- RecentActivity
- RecentUsers
- SerialNumber
- Status
- TPMFirmwareVersion
- VolumeLevel(%)
- WANIPAddress
- CPUTemperature
  - Core0(C)
  - Core1(C)
  - IwlWifi_1(C)
  - PackageId0(C)