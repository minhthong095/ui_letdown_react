//
//  PaddingLabel.swift
//  ui_letdown_react
//
//  Created by Thong on 10/09/2019.
//  Copyright © 2019 Facebook. All rights reserved.
//

class PaddingLabel: UILabel {
  
  private var _padding: UIEdgeInsets = .zero

  init(padding: UIEdgeInsets) {
    super.init(frame: .zero)
    _padding = padding
  }
  
  override func drawText(in rect: CGRect) {
    super.drawText(in: rect.inset(by: _padding))
  }
  
  override var intrinsicContentSize: CGSize {
    let size = super.intrinsicContentSize
    return CGSize(width: size.width + _padding.left + _padding.right,
                  height: size.height + _padding.top + _padding.bottom)
  }
  
  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }
}
