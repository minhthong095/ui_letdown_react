//
//  BarCodeCameraViewManager.swift
//  ui_letdown_react
//
//  Created by Thong on 10/09/2019.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation

fileprivate class _BarCodeCamera: UIView {
  
  private let _lbl = UILabel()
  private let _lblBarCodeTypes = UILabel()
  private let _lblFlash = UILabel()
  private let _lblCropData = UILabel()
  private let _btn = UIButton()
  
  private let LABEL_BARCODE_NUM = "NUM OF BARCODE TYPE: "
  private let LABEL_FLASH = "IS FLASH: "
  private let LABEL_CROP = "CROP DATA: "
  
  @objc var barcodeTypes = [String]() { didSet { setBarcodeTypeNum() } }
  @objc var flash = "INIT" { didSet { setIsFlash() } }
  @objc var cropData: String? = nil { didSet { setCropData() } }
  
  private var _onBarCodeRead: RCTDirectEventBlock?
  @objc(setOnBarCodeRead:)
  func setOnBarCodeRead(onBarCodeRead: RCTDirectEventBlock?) { _onBarCodeRead = onBarCodeRead }
  
  init() {
    super.init(frame: .zero)
    initialize()
  }
  
  private func initialize() {
    addSubview(_lbl)
    _lbl.text = "MOCK BARCODE CAMERAVIEW"
    _lbl.textColor = .white
    _lbl.translatesAutoresizingMaskIntoConstraints = false
    NSLayoutConstraint.activate([
      _lbl.centerXAnchor.constraint(equalTo: centerXAnchor),
      NSLayoutConstraint(item: _lbl, attribute: .top, relatedBy: .equal, toItem: safeAreaLayoutGuide, attribute: .centerY, multiplier: 0.3, constant: 1)
      ])
    
    addSubview(_lblBarCodeTypes)
    setBarcodeTypeNum()
    _lblBarCodeTypes.textColor = .white
    _lblBarCodeTypes.translatesAutoresizingMaskIntoConstraints = false
    NSLayoutConstraint.activate([
      _lblBarCodeTypes.topAnchor.constraint(equalTo: _lbl.centerYAnchor, constant: 20),
      _lblBarCodeTypes.centerXAnchor.constraint(equalTo: centerXAnchor)
      ])
    
    addSubview(_lblFlash)
    setIsFlash()
    _lblFlash.textColor = .white
    _lblFlash.translatesAutoresizingMaskIntoConstraints = false
    NSLayoutConstraint.activate([
      _lblFlash.topAnchor.constraint(equalTo: _lblBarCodeTypes.centerYAnchor, constant: 20),
      _lblFlash.centerXAnchor.constraint(equalTo: centerXAnchor)
      ])
    
    addSubview(_lblCropData)
    _lblCropData.text = "CROP DATA: "
    _lblCropData.textColor = .white
    _lblCropData.translatesAutoresizingMaskIntoConstraints = false
    NSLayoutConstraint.activate([
      _lblCropData.topAnchor.constraint(equalTo: _lblFlash.centerYAnchor, constant: 20),
      _lblCropData.centerXAnchor.constraint(equalTo: centerXAnchor)
      ])
    
    addSubview(_btn)
    _btn.translatesAutoresizingMaskIntoConstraints = false
    _btn.backgroundColor = .white
    _btn.setTitleColor(.black, for: .normal)
    _btn.contentEdgeInsets = UIEdgeInsets(top: 10, left: 10, bottom: 10, right: 10)
    _btn.addTarget(self, action: #selector(pressGenerateQRResult(_:)), for: .touchUpInside)
    _btn.setTitle("Generate QR Result", for: .normal)
    NSLayoutConstraint.activate([
      _btn.topAnchor.constraint(equalTo: _lblCropData.centerYAnchor, constant: 40),
      _btn.centerXAnchor.constraint(equalTo: centerXAnchor)
      ])
  }
  
  @objc private func pressGenerateQRResult(_ button: UIButton) {
    _onBarCodeRead?(["result": Int.random(in: 0...99)])
  }
  
  func pressOnTouchCrop() {
    let alert = UIAlertController(title: "", message: "Touch crop in native code.", preferredStyle: .alert)
    alert.addAction(UIAlertAction(title: "Ok", style: .cancel, handler: nil))
    UIApplication.shared.keyWindow?.rootViewController?.present(alert, animated: true, completion: nil)
  }
  
  private func setBarcodeTypeNum() {
    _lblBarCodeTypes.text = LABEL_BARCODE_NUM + String(barcodeTypes.count)
  }
  
  private func setIsFlash() {
    _lblFlash.text = LABEL_FLASH + flash
  }
  
  private func setCropData() {
    var result = ""
    if cropData == nil {
      result = LABEL_CROP
    } else {
      result = LABEL_CROP + cropData!
    }
    _lblCropData.text = result
  }
  
  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
    initialize()
  }
}


@objc(BarCodeCameraManager)
class BarCodeCameraManager: RCTViewManager {
  
  override func view() -> UIView! {
    return _BarCodeCamera()
  }
  
  @objc func touchCrop(_ reactTag: NSNumber) {
    bridge.uiManager.addUIBlock { (manager, registry) in
      if let registry = registry, let view = registry[reactTag], let realView = view as? _BarCodeCamera {
        realView.pressOnTouchCrop()
      }
    }
  }
  
  @objc
  override static func requiresMainQueueSetup() -> Bool {
    return true
  }
}
